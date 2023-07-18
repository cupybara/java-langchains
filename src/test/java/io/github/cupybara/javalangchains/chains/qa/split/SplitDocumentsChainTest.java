package io.github.cupybara.javalangchains.chains.qa.split;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.EncodingType;

import io.github.cupybara.javalangchains.util.PromptConstants;

/**
 * Unit tests for the {@link SplitDocumentsChain}
 */
class SplitDocumentsChainTest {

	private static List<Map<String, String>> documents;

	@BeforeAll
	static void beforeAll() {
		documents = new LinkedList<>();

		final Map<String, String> firstDocument = new LinkedHashMap<>();
		firstDocument.put(PromptConstants.SOURCE, "book of john");
		firstDocument.put(PromptConstants.CONTENT, "This is a short text. This is another short text.");
		documents.add(firstDocument);

		final Map<String, String> secondDocument = new LinkedHashMap<>();
		secondDocument.put(PromptConstants.SOURCE, "book of jane");
		secondDocument.put(PromptConstants.CONTENT, "This is a short text.");
		documents.add(secondDocument);
	}

	/**
	 * tests the {@link SplitDocumentsChain} using a {@link JtokkitTextSplitter}
	 */
	@Test
	void testSplitDocumentsByTokenCount() {

		/*
		 * We create a TextSplitter that splits a text into partitions using a JTokkit
		 * Encoding. We use the cl100k_base encoding which is the default for
		 * gpt-3.5-turbo.
		 */
		final TextSplitter tiktokenTextSplitter = new JtokkitTextSplitter(
				Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE), 10);

		/*
		 * we now instantiate the SplitDocumentsChain which will split our documents
		 * using the above created TextSplitter on the "content" field.
		 */
		final SplitDocumentsChain splitDocumentsChain = new SplitDocumentsChain(tiktokenTextSplitter);

		// perform the split and collect the items
		final List<Map<String, String>> splitDocuments = splitDocumentsChain.run(documents.stream())
				.collect(Collectors.toList());

		System.out.println(splitDocuments);
		assertNotNull(splitDocuments, "null result");
		assertEquals(3, splitDocuments.size(), "wrong result size");

		final Map<String, String> firstDocument = splitDocuments.get(0);
		assertEquals("This is a short text. ", firstDocument.get(PromptConstants.CONTENT),
				"wrong first chunk of split document");
		assertEquals("book of john", firstDocument.get(PromptConstants.SOURCE), "wrong source for firstDocument");

		final Map<String, String> secondDocument = splitDocuments.get(1);
		assertEquals("This is another short text.", secondDocument.get(PromptConstants.CONTENT),
				"wrong second chunk of split document");
		assertEquals("book of john", secondDocument.get(PromptConstants.SOURCE), "wrong source for secondDocument");

		final Map<String, String> thirdDocument = splitDocuments.get(2);
		assertEquals("This is a short text.", thirdDocument.get(PromptConstants.CONTENT),
				"wrong content for thirdDocument");
		assertEquals("book of jane", thirdDocument.get(PromptConstants.SOURCE), "wrong source for thirdDocument");
	}
}
