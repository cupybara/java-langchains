package com.github.hakenadu.javalangchains.chains.llm.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for the response body of an OpenAI /chat/completions request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OpenAiChatCompletionsResponse extends OpenAiChatParameters {

	/**
	 * All contained {@link OpenAiChatCompletionsChoice} instances of the response.
	 * Nested in each {@link OpenAiChatCompletionsChoice} is an instance of the
	 * response {@link OpenAiChatMessage}.
	 */
	private final List<OpenAiChatCompletionsChoice> choices;

	/**
	 * @param choices {@link #choices}
	 */
	@JsonCreator(mode = Mode.PROPERTIES)
	public OpenAiChatCompletionsResponse(final @JsonProperty("choices") List<OpenAiChatCompletionsChoice> choices) {
		this.choices = choices;
	}

	/**
	 * @return {@link #choices}
	 */
	public List<OpenAiChatCompletionsChoice> getChoices() {
		return choices;
	}
}
