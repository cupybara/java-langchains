package com.github.hakenadu.javalangchain.chains.retrieval;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.github.hakenadu.javalangchain.chains.Chain;
import com.github.hakenadu.javalangchain.util.PromptConstants;

/**
 * {@link Chain} that takes documents as input and summarizes them using a llm
 * chain that is passed as a constructor param.
 */
public class SummarizeDocumentsChain implements Chain<Stream<Map<String, String>>, Stream<Map<String, String>>> {

	/**
	 * this {@link Chain} is used to summarize each document (this will be a llm
	 * chain by default)
	 */
	private final Chain<Map<String, String>, String> documentChain;

	/**
	 * if true the {@link #documentChain} is called for each document on another
	 * thread to increase overall performance
	 */
	private final boolean parallel;

	/**
	 * @param documentChain {@link #documentChain}
	 * @param parallel      {@link #parallel}
	 */
	public SummarizeDocumentsChain(final Chain<Map<String, String>, String> documentChain, final boolean parallel) {
		this.documentChain = documentChain;
		this.parallel = parallel;
	}

	/**
	 * @param documentChain {@link #documentChain}
	 */
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
