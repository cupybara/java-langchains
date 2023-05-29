package com.github.hakenadu.javalangchain.chains.retrieval;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;

import com.github.hakenadu.javalangchain.chains.Chain;
import com.github.hakenadu.javalangchain.util.PromptTemplates;

public class CombineDocumentsChain implements Chain<RetrievedDocuments, Map<String, String>> {

	private final String documentPromptTemplate;

	public CombineDocumentsChain(final String documentPromptTemplate) {
		this.documentPromptTemplate = documentPromptTemplate;
	}

	public CombineDocumentsChain() {
		this(PromptTemplates.QA_DOCUMENT);
	}

	@Override
	public Map<String, String> run(final RetrievedDocuments input) {
		final String summaries = input.getDocuments().map(this::createDocumentPrompt)
				.collect(Collectors.joining("\n\n"));

		final Map<String, String> result = new HashMap<>();
		result.put("question", input.getQuestion());
		result.put("summaries", summaries);
		return result;
	}

	private String createDocumentPrompt(final Map<String, String> document) {
		return new StringSubstitutor(document).replace(documentPromptTemplate);
	}
}
