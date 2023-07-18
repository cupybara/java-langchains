package io.github.cupybara.javalangchains.chains.llm.openai.completions;

/**
 * Model class for the OpenAI /completions request body
 */
public final class OpenAiCompletionsRequest extends OpenAiCompletionsParameters {

	/**
	 * The prompt for the model
	 */
	private final String prompt;

	/**
	 * @param prompt {@link #prompt}
	 */
	public OpenAiCompletionsRequest(final String prompt) {
		this.prompt = prompt;
	}

	/**
	 * @return {@link #prompt}
	 */
	public String getPrompt() {
		return prompt;
	}
}
