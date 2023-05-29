package com.github.hakenadu.javalangchain.chains.retrieval;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.github.hakenadu.javalangchain.chains.Chain;
import com.github.hakenadu.javalangchain.util.PromptConstants;

public class SummarizeDocumentsChain implements Chain<Stream<Map<String, String>>, Stream<Map<String, String>>> {

	private final Chain<Map<String, String>, String> documentChain;
	private final boolean parallel;

	public SummarizeDocumentsChain(final Chain<Map<String, String>, String> documentChain, final boolean parallel) {
		this.documentChain = documentChain;
		this.parallel = parallel;
	}

	public SummarizeDocumentsChain(final Chain<Map<String, String>, String> documentChain) {
		this(documentChain, true);
	}

	@Override
	public Stream<Map<String, String>> run(final Stream<Map<String, String>> input) {
		final Stream<Map<String, String>> stream = input.map(document -> {
			LOGGER.debug("pre summarization: {}", document);
			final String mappedContent = documentChain.run(document);
			LOGGER.debug("post summarization: {}", mappedContent);

			final Map<String, String> mappedDocument = new HashMap<>(document);
			mappedDocument.put(PromptConstants.CONTENT, mappedContent);
			return mappedDocument;
		});

		if (parallel) {
			return stream.parallel();
		}

		return stream;
	}
}
