package io.github.cupybara.javalangchains.chains.data.read;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.cupybara.javalangchains.chains.data.reader.ReadDocumentsFromInMemoryPdfChain;
import io.github.cupybara.javalangchains.chains.data.reader.ReadDocumentsFromInMemoryPdfChain.InMemoryPdf;
import io.github.cupybara.javalangchains.chains.data.reader.ReadDocumentsFromPdfChainBase.PdfReadMode;
import io.github.cupybara.javalangchains.util.PromptConstants;

/**
 * Unit Tests for the {@link ReadDocumentsFromInMemoryPdfChainTest}
 */
class ReadDocumentsFromInMemoryPdfChainTest {

	private static InMemoryPdf inMemoryPdf;

	@BeforeAll
	static void setupBeforeAll() throws URISyntaxException, IOException {
		inMemoryPdf = new InMemoryPdf(
				IOUtils.toByteArray(
						ReadDocumentsFromInMemoryPdfChainTest.class.getResourceAsStream("/pdf/qa/book-of-john-3.pdf")),
				"my-in-memory.pdf");
	}

	@Test
	void testReadWhole() {
		final List<Map<String, String>> documents = new ReadDocumentsFromInMemoryPdfChain(PdfReadMode.WHOLE)
				.run(inMemoryPdf).collect(Collectors.toList());
		assertEquals(1, documents.size(), "incorrect number of read documents");

		final Map<String, String> doc = documents.get(0);
		assertNotNull(doc.get(PromptConstants.CONTENT), "got no content for doc");
		assertEquals("my-in-memory.pdf", doc.get(PromptConstants.SOURCE), "got wrong source for doc");
	}

	@Test
	void testReadPages() {
		final List<Map<String, String>> documents = new ReadDocumentsFromInMemoryPdfChain(PdfReadMode.PAGES)
				.run(inMemoryPdf).collect(Collectors.toList());
		assertEquals(2, documents.size(), "incorrect number of read document pages");

		final Map<String, String> doc1 = documents.get(0);
		assertNotNull(doc1.get(PromptConstants.CONTENT), "got no content for doc1");
		assertEquals("my-in-memory.pdf p.1", doc1.get(PromptConstants.SOURCE), "got wrong source for doc1");

		final Map<String, String> doc2 = documents.get(1);
		assertNotNull(doc2.get(PromptConstants.CONTENT), "got no content for doc2");
		assertEquals("my-in-memory.pdf p.2", doc2.get(PromptConstants.SOURCE), "got wrong source for doc2");
	}
}
