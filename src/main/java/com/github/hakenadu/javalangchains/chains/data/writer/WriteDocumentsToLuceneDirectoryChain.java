package com.github.hakenadu.javalangchains.chains.data.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * Stores documents in a lucene {@link Directory}
 */
public class WriteDocumentsToLuceneDirectoryChain implements Chain<Stream<Map<String, String>>, Directory> {

	/**
	 * The directory {@link Path} used to store the created index data
	 */
	private final Path directoryOutputPath;

	/**
	 * @param directoryOutputPath {@link #directoryOutputPath}
	 */
	public WriteDocumentsToLuceneDirectoryChain(final Path directoryOutputPath) {
		this.directoryOutputPath = directoryOutputPath;
	}

	/**
	 * creates a {@link WriteDocumentsToLuceneDirectoryChain} with a default temp
	 * directory path
	 * 
	 * @throws IOException on error creating the temp dir
	 */
	public WriteDocumentsToLuceneDirectoryChain() throws IOException {
		this(Files.createTempDirectory("lucene"));
	}

	@Override
	public Directory run(final Stream<Map<String, String>> input) {
		final Directory indexDirectory;
		try {
			indexDirectory = new MMapDirectory(this.directoryOutputPath);
		} catch (final IOException ioException) {
			throw new IllegalStateException("error creating index", ioException);
		}

		final StandardAnalyzer analyzer = new StandardAnalyzer();
		final IndexWriterConfig config = new IndexWriterConfig(analyzer);

		try (final IndexWriter indexWriter = new IndexWriter(indexDirectory, config)) {

			input.forEach(document -> {
				final Document doc = new Document();
				doc.add(new TextField(PromptConstants.CONTENT, document.get(PromptConstants.CONTENT), Field.Store.YES));
				doc.add(new StringField(PromptConstants.SOURCE, document.get(PromptConstants.SOURCE), Field.Store.YES));
				try {
					indexWriter.addDocument(doc);
				} catch (final IOException innerIoException) {
					throw new IllegalStateException("error writing document: " + document, innerIoException);
				}
			});

			indexWriter.commit();
		} catch (final IOException ioException) {
			throw new IllegalStateException("error creating writer", ioException);
		}

		return indexDirectory;
	}
}
