package com.github.hakenadu.javalangchain.chains.llm;

import java.util.Map;

import com.github.hakenadu.javalangchain.chains.Chain;

public abstract class LargeLanguageModelChain implements Chain<Map<String, String>, String> {

	private final String promptTemplate;

	protected LargeLanguageModelChain(final String promptTemplate) {
		this.promptTemplate = promptTemplate;
	}

	protected final String getPromptTemplate() {
		return promptTemplate;
	}
}
