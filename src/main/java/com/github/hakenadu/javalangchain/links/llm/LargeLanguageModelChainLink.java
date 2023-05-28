package com.github.hakenadu.javalangchain.links.llm;

import java.util.Map;

import com.github.hakenadu.javalangchain.links.ChainLink;

public abstract class LargeLanguageModelChainLink implements ChainLink<Map<String, String>, String> {

	private final String promptTemplate;

	protected LargeLanguageModelChainLink(final String promptTemplate) {
		this.promptTemplate = promptTemplate;
	}

	protected final String getPromptTemplate() {
		return promptTemplate;
	}
}
