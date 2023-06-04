package com.github.hakenadu.javalangchains.chains.retrieval;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;

import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * This {@link RetrievalChain} retrieves documents from a lucene index
 */
public class LuceneRetrievalChain extends RetrievalChain implements Closeable {

	private final Function<String, Query> queryCreator;
	private final Function<Document, Map<String, String>> documentCreator;

	private final IndexReader indexReader;
	private final IndexSearcher indexSearcher;

	/**
	 * Creates an instance of {@link LuceneRetrievalChain}
	 * 
	 * @param indexDirectory   Lucene Index {@link Directory}
	 * @param maxDocumentCount maximal count of retrieved documents
	 * @param queryCreator     this {@link Function} accepts the user's question and
	 *                         provides the {@link Query} which is executed against
	 *                         the Lucene {@link Directory}
	 * @param documentCreator  this {@link Function} accepts a lucene
	 *                         {@link Document} and provides a {@link Map} of key
	 *                         value pairs for subsequent chains
	 */
	public LuceneRetrievalChain(final Directory indexDirectory, final int maxDocumentCount,
			final Function<String, Query> queryCreator, final Function<Document, Map<String, String>> documentCreator) {
		super(maxDocumentCount);
		this.queryCreator = queryCreator;
		this.documentCreator = documentCreator;

		try {
			this.indexReader = DirectoryReader.open(indexDirectory);
		} catch (final IOException ioException) {
			throw new IllegalStateException("could not open indexReader", ioException);
		}

		this.indexSearcher = new IndexSearcher(indexReader);
		this.indexSearcher.setSimilarity(new BM25Similarity()); // TODO: Parameterize
	}

	/**
	 * Creates an instance of {@link LuceneRetrievalChain}. Uses
	 * {@link #createDocument(Document)} to map all lucene document fields into the
	 * output {@link Map}.
	 * 
	 * @param indexDirectory   Lucene Index {@link Directory}
	 * @param maxDocumentCount maximal count of retrieved documents
	 * @param queryCreator     this {@link Function} accepts the user's question and
	 *                         provides the {@link Query} which is executed against
	 *                         the Lucene {@link Directory}
	 */
	public LuceneRetrievalChain(final Directory indexDirectory, final int maxDocumentCount,
			final Function<String, Query> queryCreator) {
		this(indexDirectory, maxDocumentCount, queryCreator, LuceneRetrievalChain::createDocument);
	}

	/**
	 * Creates an instance of {@link LuceneRetrievalChain}. Uses
	 * {@link #createQuery(String)} to provide a default {@link Query} using a
	 * {@link StandardAnalyzer} targeting the field
	 * {@link PromptConstants#CONTENT}.. Uses {@link #createDocument(Document)} to
	 * map all lucene document fields into the output {@link Map}.
	 * 
	 * @param indexDirectory   Lucene Index {@link Directory}
	 * @param maxDocumentCount maximal count of retrieved documents
	 */
	public LuceneRetrievalChain(final Directory indexDirectory, final int maxDocumentCount) {
		this(indexDirectory, maxDocumentCount, LuceneRetrievalChain::createQuery, LuceneRetrievalChain::createDocument);
	}

	/**
	 * Creates an instance of {@link LuceneRetrievalChain} with a maximum of 4
	 * retrieved documents. Uses {@link #createQuery(String)} to provide a default
	 * {@link Query} using a {@link StandardAnalyzer} targeting the field
	 * {@link PromptConstants#CONTENT}.. Uses {@link #createDocument(Document)} to
	 * map all lucene document fields into the output {@link Map}.
	 * 
	 * @param indexDirectory Lucene Index {@link Directory}
	 */
	public LuceneRetrievalChain(final Directory indexDirectory) {
		this(indexDirectory, 4);
	}

	@Override
	public Stream<Map<String, String>> run(final String input) {
		final Query query = queryCreator.apply(input);

		final TopDocs topDocs;
		try {
			topDocs = indexSearcher.search(query, this.getMaxDocumentCount());
		} catch (final IOException ioException) {
			throw new IllegalStateException("error processing search for query " + query, ioException);
		}

		return Arrays.stream(topDocs.scoreDocs).map(hit -> {
			try {
				return indexSearcher.doc(hit.doc);
			} catch (final IOException ioException) {
				throw new IllegalStateException("could not process document " + hit.doc, ioException);
			}
		}).map(this.documentCreator).map(document -> {
			final Map<String, String> mappedDocument = new LinkedHashMap<>(document);
			mappedDocument.put(PromptConstants.QUESTION, input);
			return mappedDocument;
		});
	}

	@Override
	public void close() throws IOException {
		this.indexReader.close();
	}

	private static Map<String, String> createDocument(final Document document) {
		return document.getFields().stream()
				.collect(Collectors.toMap(IndexableField::name, IndexableField::stringValue));
	}

	private static Query createQuery(final String searchTerm) {
		final StandardAnalyzer analyzer = new StandardAnalyzer();
		final QueryParser queryParser = new QueryParser(PromptConstants.CONTENT, analyzer);
		try {
			return queryParser.parse(searchTerm);
		} catch (final ParseException parseException) {
			throw new IllegalStateException("could not create query for searchTerm " + searchTerm, parseException);
		}
	}
}
