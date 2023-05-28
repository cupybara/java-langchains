package com.github.hakenadu.javalangchain.chains.llm.openai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class OpenAiChatCompletionsChoice {

	private final OpenAiChatMessage message;

	@JsonCreator(mode = Mode.PROPERTIES)
	public OpenAiChatCompletionsChoice(final @JsonProperty("message") OpenAiChatMessage message) {
		this.message = message;
	}

	public OpenAiChatMessage getMessage() {
		return message;
	}
}
