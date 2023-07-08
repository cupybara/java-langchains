package com.github.hakenadu.javalangchains.chains.data.retrieval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * Tests for the {@link LuceneRetrievalChain}
 */
class LuceneRetrievalChainTest {

	private static Path tempDirPath;
	private static Directory directory;

	@BeforeAll
	static void beforeAll() throws IOException {
		tempDirPath = Files.createTempDirectory("lucene");
		directory = new MMapDirectory(tempDirPath);
		fillDirectory(directory);
	}

	static void fillDirectory(final Directory indexDirectory) throws IOException {
		final StandardAnalyzer analyzer = new StandardAnalyzer();
		final IndexWriterConfig config = new IndexWriterConfig(analyzer);
		try (final IndexWriter indexWriter = new IndexWriter(indexDirectory, config)) {
			for (final String content : DocumentTestUtil.DOCUMENTS) {
				final Document doc = new Document();
				doc.add(new TextField(PromptConstants.CONTENT, content, Field.Store.YES));
				doc.add(new StringField(PromptConstants.SOURCE, String.valueOf(DocumentTestUtil.DOCUMENTS.indexOf(content) + 1),
						Field.Store.YES));
				indexWriter.addDocument(doc);
			}

			indexWriter.commit();
		}
	}

	@AfterAll
	static void afterAll() throws IOException {
		directory.close();
		Files.walk(tempDirPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

	@Test
	void testRun() throws IOException {
		try (final LuceneRetrievalChain retrievalChain = new LuceneRetrievalChain(directory, 2)) {
			final String question = "what kind of art does john make?";

			final List<Map<String, String>> documents = retrievalChain.run(question).collect(Collectors.toList());
			assertFalse(documents.isEmpty(), "no documents retrieved");

			final Map<String, String> mostRelevantDocument = documents.get(0);
			assertTrue(mostRelevantDocument.containsKey(PromptConstants.SOURCE), "source key is missing");
			assertEquals("2", mostRelevantDocument.get(PromptConstants.SOURCE), "invalid source");

			assertTrue(mostRelevantDocument.containsKey(PromptConstants.CONTENT), "content key is missing");
			assertEquals(DocumentTestUtil.DOCUMENT_2, mostRelevantDocument.get(PromptConstants.CONTENT), "invalid content");
		}
	}
}
