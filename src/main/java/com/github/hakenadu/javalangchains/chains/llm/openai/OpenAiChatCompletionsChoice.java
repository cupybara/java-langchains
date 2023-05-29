package com.github.hakenadu.javalangchains.chains.llm.openai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for choices in an OpenAI /chat/completions response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OpenAiChatCompletionsChoice {

	/**
	 * the {@link OpenAiChatMessage} for this response choice
	 */
	private final OpenAiChatMessage message;

	/**
	 * @param message {@link #message}
	 */
	@JsonCreator(mode = Mode.PROPERTIES)
	public OpenAiChatCompletionsChoice(final @JsonProperty("message") OpenAiChatMessage message) {
		this.message = message;
	}

	/**
	 * @return {@link #message}
	 */
	public OpenAiChatMessage getMessage() {
		return message;
	}
}
