package com.github.hakenadu.javalangchains.chains.llm.openai;

import java.util.List;

/**
 * Model class for the OpenAI /chat/completions request body
 */
public final class OpenAiChatCompletionsRequest extends OpenAiChatParameters {

	/**
	 * The {@link OpenAiChatMessage} instances of the conversation
	 */
	private final List<OpenAiChatMessage> messages;

	/**
	 * @param messages {@link #messages}
	 */
	public OpenAiChatCompletionsRequest(final List<OpenAiChatMessage> messages) {
		this.messages = messages;
	}

	/**
	 * @return {@link #messages}
	 */
	public List<OpenAiChatMessage> getMessages() {
		return messages;
	}
}
