package io.github.cupybara.javalangchains.chains.llm;

import java.util.Map;

import io.github.cupybara.javalangchains.chains.Chain;

/**
 * Parent of all {@link Chain Chains} which allow passing input to a large
 * language model. Accepts a document of key value pairs and provides the LLM
 * output.
 */
public abstract class LargeLanguageModelChain implements Chain<Map<String, String>, String> {

	/**
	 * The template which contains placeholders in the form ${myPlaceholder} that
	 * are replaced for input documents before creating a request to a LLM.
	 */
	private final String promptTemplate;

	/**
	 * creates an instance of the {@link LargeLanguageModelChain}
	 * 
	 * @param promptTemplate {@link #promptTemplate}
	 */
	protected LargeLanguageModelChain(final String promptTemplate) {
		this.promptTemplate = promptTemplate;
	}

	/**
	 * @return {@link #promptTemplate}
	 */
	protected final String getPromptTemplate() {
		return promptTemplate;
	}
}
