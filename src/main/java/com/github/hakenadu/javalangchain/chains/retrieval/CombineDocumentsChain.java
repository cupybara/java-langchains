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

public class CombineDocumentsChain implements Chain<Stream<Map<String, String>>, Map<String, String>> {

	private final String documentPromptTemplate;

	public CombineDocumentsChain(final String documentPromptTemplate) {
		this.documentPromptTemplate = documentPromptTemplate;
	}

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
