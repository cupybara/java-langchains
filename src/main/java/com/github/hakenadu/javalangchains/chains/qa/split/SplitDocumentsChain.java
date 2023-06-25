package com.github.hakenadu.javalangchains.chains.qa.split;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * This {@link Chain} is used to split long documents into chunks. All document
 * keys are copied except for the {@link PromptConstants#CONTENT} which is
 * split.
 */
public class SplitDocumentsChain implements Chain<Stream<Map<String, String>>, Stream<Map<String, String>>> {

	/**
	 * This {@link TextSplitter} is used to create one or more documents from an
	 * input document based on the {@link PromptConstants#CONTENT} key.
	 */
	private final TextSplitter textSplitter;

	/**
	 * creates an instance of the {@link SplitDocumentsChain}
	 * 
	 * @param textSplitter {@link #textSplitter}
	 */
	public SplitDocumentsChain(final TextSplitter textSplitter) {
		this.textSplitter = textSplitter;
	}

	@Override
	public Stream<Map<String, String>> run(final Stream<Map<String, String>> input) {
		return input.flatMap(this::splitDocument);
	}

	private Stream<Map<String, String>> splitDocument(final Map<String, String> document) {
		final String content = document.get(PromptConstants.CONTENT);

		return this.textSplitter.split(content).stream().map(contentPart -> {
			final Map<String, String> documentPart = new HashMap<>(document);
			documentPart.put(PromptConstants.CONTENT, contentPart);
			return documentPart;
		});
	}
}
