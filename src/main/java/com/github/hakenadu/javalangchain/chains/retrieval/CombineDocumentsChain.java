package com.github.hakenadu.javalangchain.chains.retrieval;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.StringSubstitutor;

import com.github.hakenadu.javalangchain.chains.Chain;
import com.github.hakenadu.javalangchain.util.PromptConstants;
import com.github.hakenadu.javalangchain.util.PromptTemplates;

/**
 * This {@link Chain} is used to combine multiple retrieved documents into one
 * prompt which can then be used to target a LLM in subsequent steps.
 */
public class CombineDocumentsChain implements Chain<Stream<Map<String, String>>, Map<String, String>> {

	/**
	 * The template for each single document which contains placeholders in the form
	 * ${myPlaceholder} that are replaced for each the keys of each input document.
	 */
	private final String documentPromptTemplate;

	/**
	 * creates an instance of the {@link CombineDocumentsChain}
	 * 
	 * @param documentPromptTemplate {@link #documentPromptTemplate}
	 */
	public CombineDocumentsChain(final String documentPromptTemplate) {
		this.documentPromptTemplate = documentPromptTemplate;
	}

	/**
	 * creates an instance of the {@link CombineDocumentsChain}
	 */
	public CombineDocumentsChain() {
		this(PromptTemplates.QA_DOCUMENT);
	}

	@Override
	public Map<String, String> run(final Stream<Map<String, String>> input) {
		final AtomicReference<String> questionRef = new AtomicReference<>();

		final String combinedContent = input.map(document -> {
			if (questionRef.get() == null) {
				questionRef.set(document.get(PromptConstants.QUESTION));
			}
			return this.createDocumentPrompt(document);
		}).collect(Collectors.joining("\n\n"));

		final Map<String, String> result = new HashMap<>();
		result.put("question", questionRef.get());
		result.put("content", combinedContent);
		return result;
	}

	private String createDocumentPrompt(final Map<String, String> document) {
		return new StringSubstitutor(document).replace(documentPromptTemplate);
	}
}
