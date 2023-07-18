package io.github.cupybara.javalangchains.chains.llm.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model class for the response body of an OpenAI request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class OpenAiResponse<C> {

	/**
	 * All contained choice instances of the response.
	 */
	private final List<C> choices;

	/**
	 * @param choices {@link #choices}
	 */
	protected OpenAiResponse(final List<C> choices) {
		this.choices = choices;
	}

	/**
	 * @return {@link #choices}
	 */
	public List<C> getChoices() {
		return choices;
	}
}
