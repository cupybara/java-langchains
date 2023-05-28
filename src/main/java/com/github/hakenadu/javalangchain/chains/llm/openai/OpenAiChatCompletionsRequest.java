package com.github.hakenadu.javalangchain.chains.llm.openai;

import java.util.List;

public final class OpenAiChatCompletionsRequest extends OpenAiChatParameters {

	private final List<OpenAiChatMessage> messages;

	public OpenAiChatCompletionsRequest(final List<OpenAiChatMessage> messages) {
		this.messages = messages;
	}

	public List<OpenAiChatMessage> getMessages() {
		return messages;
	}
}
