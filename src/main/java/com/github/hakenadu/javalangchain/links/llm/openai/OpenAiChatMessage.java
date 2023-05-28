package com.github.hakenadu.javalangchain.links.llm.openai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;

final class OpenAiChatMessage {

	private final String role;
	private final String content;

	@JsonCreator(mode = Mode.PROPERTIES)
	public OpenAiChatMessage(final @JsonProperty("role") String role, final @JsonProperty("content") String content) {
		this.role = role;
		this.content = content;
	}

	public String getRole() {
		return role;
	}

	public String getContent() {
		return content;
	}
}
