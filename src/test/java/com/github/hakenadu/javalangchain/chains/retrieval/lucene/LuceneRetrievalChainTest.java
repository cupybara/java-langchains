package com.github.hakenadu.javalangchain.chains.retrieval.lucene;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link LuceneRetrievalChain}
 */
class LuceneRetrievalChainTest {

	private static Directory directory;

	@BeforeAll
	public static void setupBeforeAll() throws IOException {
		directory = new RAMDirectory();

		final StandardAnalyzer analyzer = new StandardAnalyzer();
		final IndexWriterConfig config = new IndexWriterConfig(analyzer);
		try (final IndexWriter indexWriter = new IndexWriter(directory, config)) {
			final List<String> documents = Arrays.asList("Dies ist das erste Dokument.",
					"Hier ist das zweite Dokument.", "Und dies ist das dritte Dokument.");

			for (final String content : documents) {
				Document doc = new Document();
				doc.add(new StringField("id", UUID.randomUUID().toString(), Field.Store.YES));
				doc.add(new TextField("content", content, Field.Store.YES));
				indexWriter.addDocument(doc);
			}
		}

	}

	@Test
	public void testRun() {

	}
}
