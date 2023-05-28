package com.github.hakenadu.javalangchain.chains.llm.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class OpenAiChatCompletionsResponse extends OpenAiChatParameters {

	private final List<OpenAiChatCompletionsChoice> choices;

	@JsonCreator(mode = Mode.PROPERTIES)
	public OpenAiChatCompletionsResponse(final @JsonProperty("choices") List<OpenAiChatCompletionsChoice> choices) {
		this.choices = choices;
	}

	public List<OpenAiChatCompletionsChoice> getChoices() {
		return choices;
	}
}
