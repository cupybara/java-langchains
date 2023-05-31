package com.github.hakenadu.javalangchains.chains.llm.openai.completions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for choices in an OpenAI /completions response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OpenAiCompletionsChoice {

	/**
	 * the completion result
	 */
	private final String text;

	/**
	 * @param text {@link #text}
	 */
	@JsonCreator(mode = Mode.PROPERTIES)
	public OpenAiCompletionsChoice(final @JsonProperty("text") String text) {
		this.text = text;
	}

	/**
	 * @return {@link #text}
	 */
	public String getText() {
		return text;
	}
}
