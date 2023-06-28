package com.github.hakenadu.javalangchains.chains.qa;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * {@link Chain} that takes documents as input and modifies their
 * {@link PromptConstants#CONTENT} entry using a llm chain that is passed as a
 * constructor param.
 */
public class ModifyDocumentsContentChain implements Chain<Stream<Map<String, String>>, Stream<Map<String, String>>> {

	/**
	 * this {@link Chain} is applied each document (a LLM Chain for example)
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
	public ModifyDocumentsContentChain(final Chain<Map<String, String>, String> documentChain, final boolean parallel) {
		this.documentChain = documentChain;
		this.parallel = parallel;
	}

	/**
	 * @param documentChain {@link #documentChain}
	 */
	public ModifyDocumentsContentChain(final Chain<Map<String, String>, String> documentChain) {
		this(documentChain, true);
	}

	@Override
	public Stream<Map<String, String>> run(final Stream<Map<String, String>> input) {
		final Stream<Map<String, String>> stream = input.map(document -> {
			LogManager.getLogger(getClass()).trace("pre modification: {}", document);
			final String mappedContent = documentChain.run(document);
			LogManager.getLogger(getClass()).trace("post modification: {}", mappedContent);

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
