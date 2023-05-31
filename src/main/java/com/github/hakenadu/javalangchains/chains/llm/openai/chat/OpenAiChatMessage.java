package com.github.hakenadu.javalangchains.chains.llm.openai.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;

/**
 * Model class for request and response messages of an OpenAI /chat/completions
 * request
 */
public final class OpenAiChatMessage {

	/**
	 * system|user|assistant
	 */
	private final String role;

	/**
	 * Message text
	 */
	private final String content;

	/**
	 * @param role    {@link #role}
	 * @param content {@link #content}
	 */
	@JsonCreator(mode = Mode.PROPERTIES)
	public OpenAiChatMessage(final @JsonProperty("role") String role, final @JsonProperty("content") String content) {
		this.role = role;
		this.content = content;
	}

	/**
	 * @return {@link #role}
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @return {@link #content}
	 */
	public String getContent() {
		return content;
	}
}
