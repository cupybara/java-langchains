package com.github.hakenadu.javalangchains.chains.llm.openai.chat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hakenadu.javalangchains.chains.llm.openai.OpenAiChain;

/**
 * {@link OpenAiChain} for usage with the OpenAI /chat/completions API
 */
public class OpenAiChatCompletionsChain extends
		OpenAiChain<OpenAiChatCompletionsParameters, OpenAiChatCompletionsRequest, OpenAiChatCompletionsResponse> {

	/**
	 * The template for the system role which contains placeholders in the form
	 * ${myPlaceholder} that are replaced for input documents before creating a
	 * request to a LLM.
	 */
	private final String systemTemplate;

	/**
	 * @param promptTemplate {@link #getPromptTemplate()}
	 * @param parameters     {@link #parameters}r
	 * @param apiKey         {@link #apiKey}
	 * @param systemTemplate {@link #systemTemplate}
	 * @param objectMapper   {@link #objectMapper}
	 * @param webClient      {@link #webClient}
	 */
	public OpenAiChatCompletionsChain(final String promptTemplate, final OpenAiChatCompletionsParameters parameters,
			final String apiKey, final String systemTemplate, final ObjectMapper objectMapper,
			final WebClient webClient) {
		super(promptTemplate, "/v1/chat/completions", OpenAiChatCompletionsResponse.class, parameters, apiKey,
				objectMapper, webClient);
		this.systemTemplate = systemTemplate;
	}

	/**
	 * @param promptTemplate {@link #getPromptTemplate()}
	 * @param parameters     {@link #parameters}
	 * @param apiKey         {@link #apiKey}
	 * @param systemTemplate {@link #systemTemplate}s
	 */
	public OpenAiChatCompletionsChain(final String promptTemplate, final OpenAiChatCompletionsParameters parameters,
			final String apiKey, final String systemTemplate) {
		this(promptTemplate, parameters, apiKey, systemTemplate, createDefaultObjectMapper(), createDefaultWebClient());
	}

	/**
	 * @param promptTemplate {@link #getPromptTemplate()}
	 * @param parameters     {@link #parameters}
	 * @param apiKey         {@link #apiKey}
	 */
	public OpenAiChatCompletionsChain(final String promptTemplate, final OpenAiChatCompletionsParameters parameters,
			final String apiKey) {
		this(promptTemplate, parameters, apiKey, null);
	}

	@Override
	protected OpenAiChatCompletionsRequest createRequest(Map<String, String> input) {
		final List<OpenAiChatMessage> messages = new LinkedList<>();
		if (systemTemplate != null) {
			messages.add(new OpenAiChatMessage("system", new StringSubstitutor(input).replace(systemTemplate)));
		}
		messages.add(new OpenAiChatMessage("user", new StringSubstitutor(input).replace(getPromptTemplate())));

		return new OpenAiChatCompletionsRequest(messages);
	}

	@Override
	protected String createOutput(final OpenAiChatCompletionsResponse response) {
		return response.getChoices().get(0).getMessage().getContent();
	}
}
