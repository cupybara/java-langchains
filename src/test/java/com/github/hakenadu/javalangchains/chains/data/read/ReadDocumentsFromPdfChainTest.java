package com.github.hakenadu.javalangchains.chains.data.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchains.chains.data.reader.ReadDocumentsFromPdfChain;
import com.github.hakenadu.javalangchains.chains.data.reader.ReadDocumentsFromPdfChainBase.PdfReadMode;
import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * Unit Tests for the {@link ReadDocumentsFromPdfChainTest}
 */
class ReadDocumentsFromPdfChainTest {

	private static Path pdfDirectory;

	@BeforeAll
	static void setupBeforeAll() throws URISyntaxException {
		pdfDirectory = Paths.get(ReadDocumentsFromPdfChainTest.class.getResource("/pdf/qa").toURI());
	}

	@Test
	void testReadWhole() {
		final List<Map<String, String>> documents = new ReadDocumentsFromPdfChain(PdfReadMode.WHOLE).run(pdfDirectory)
				.collect(Collectors.toList());
		assertEquals(3, documents.size(), "incorrect number of read documents");

		final Map<String, String> doc1 = documents.get(0);
		assertNotNull(doc1.get(PromptConstants.CONTENT), "got no content for doc1");
		assertEquals("book-of-john-1.pdf", doc1.get(PromptConstants.SOURCE), "got wrong source for doc1");

		final Map<String, String> doc2 = documents.get(1);
		assertNotNull(doc2.get(PromptConstants.CONTENT), "got no content for doc2");
		assertEquals("book-of-john-2.pdf", doc2.get(PromptConstants.SOURCE), "got wrong source for doc2");

		final Map<String, String> doc3 = documents.get(2);
		assertNotNull(doc3.get(PromptConstants.CONTENT), "got no content for doc3");
		assertEquals("book-of-john-3.pdf", doc3.get(PromptConstants.SOURCE), "got wrong source for doc3");
	}

	@Test
	void testReadPages() {
		final List<Map<String, String>> documents = new ReadDocumentsFromPdfChain(PdfReadMode.PAGES).run(pdfDirectory)
				.collect(Collectors.toList());
		assertEquals(4, documents.size(), "incorrect number of read document pages");

		final Map<String, String> doc1 = documents.get(0);
		assertNotNull(doc1.get(PromptConstants.CONTENT), "got no content for doc1");
		assertFalse(doc1.get(PromptConstants.CONTENT).trim().isEmpty(), "got empty content for doc1");
		assertEquals("book-of-john-1.pdf p.1", doc1.get(PromptConstants.SOURCE), "got wrong source for doc1");

		final Map<String, String> doc2 = documents.get(1);
		assertNotNull(doc2.get(PromptConstants.CONTENT), "got no content for doc2");
		assertFalse(doc2.get(PromptConstants.CONTENT).trim().isEmpty(), "got empty content for doc2");
		assertEquals("book-of-john-2.pdf p.1", doc2.get(PromptConstants.SOURCE), "got wrong source for doc2");

		final Map<String, String> doc3 = documents.get(2);
		assertNotNull(doc3.get(PromptConstants.CONTENT), "got no content for doc3");
		assertFalse(doc3.get(PromptConstants.CONTENT).trim().isEmpty(), "got empty content for doc3");
		assertEquals("book-of-john-3.pdf p.1", doc3.get(PromptConstants.SOURCE), "got wrong source for doc3");

		final Map<String, String> doc4 = documents.get(3);
		assertNotNull(doc4.get(PromptConstants.CONTENT), "got no content for doc4");
		assertFalse(doc4.get(PromptConstants.CONTENT).trim().isEmpty(), "got empty content for doc4");
		assertEquals("book-of-john-3.pdf p.2", doc4.get(PromptConstants.SOURCE), "got wrong source for doc4");
	}
}
