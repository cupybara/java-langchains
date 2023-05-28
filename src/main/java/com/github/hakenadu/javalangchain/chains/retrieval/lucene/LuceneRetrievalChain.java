package com.github.hakenadu.javalangchain.chains.retrieval.lucene;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import com.github.hakenadu.javalangchain.chains.retrieval.Document;
import com.github.hakenadu.javalangchain.chains.retrieval.RetrievalChain;

public class LuceneRetrievalChain extends RetrievalChain implements Closeable {

	private final Function<String, Query> queryCreator;
	private final Function<org.apache.lucene.document.Document, Document> documentCreator;

	private final IndexReader indexReader;
	private final IndexSearcher indexSearcher;

	public LuceneRetrievalChain(final Directory indexDirectory, final int maxDocumentCount,
			final Function<String, Query> queryCreator,
			final Function<org.apache.lucene.document.Document, Document> documentCreator) {
		super(maxDocumentCount);
		this.queryCreator = queryCreator;
		this.documentCreator = documentCreator;

		try {
			this.indexReader = DirectoryReader.open(indexDirectory);
		} catch (final IOException ioException) {
			throw new IllegalStateException("could not open indexReader", ioException);
		}

		this.indexSearcher = new IndexSearcher(indexReader);
	}

	@Override
	public Collection<Document> run(final String input) {
		final Query query = queryCreator.apply(input);

		final TopDocs topDocs;
		try {
			topDocs = indexSearcher.search(query, this.getMaxDocumentCount());
		} catch (final IOException ioException) {
			throw new IllegalStateException("error processing search for query " + query, ioException);
		}

		final ScoreDoc[] hits = topDocs.scoreDocs;

		final List<Document> documents = new LinkedList<>();
		for (final ScoreDoc hit : hits) {
			final org.apache.lucene.document.Document doc;
			try {
				doc = indexSearcher.doc(hit.doc);
			} catch (final IOException ioException) {
				throw new IllegalStateException("could not process document " + hit.doc, ioException);
			}

			final Document document = documentCreator.apply(doc);
			documents.add(document);
		}

		return documents;
	}

	@Override
	public void close() throws IOException {
		this.indexReader.close();
	}
}
