package com.github.hakenadu.javalangchain.chains.llm.openai;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hakenadu.javalangchain.chains.llm.LargeLanguageModelChain;

/**
 * {@link LargeLanguageModelChain} 
 */
public class OpenAiChatChain extends LargeLanguageModelChain {

	private final String systemTemplate;
	private final OpenAiChatParameters parameters;
	private final String apiKey;
	private final ObjectMapper objectMapper;
	private final WebClient webClient;

	public OpenAiChatChain(final String promptTemplate, final OpenAiChatParameters parameters, final String apiKey,
			final String systemTemplate, final ObjectMapper objectMapper, final WebClient webClient) {
		super(promptTemplate);
		this.parameters = parameters;
		this.apiKey = apiKey;
		this.systemTemplate = systemTemplate;
		this.objectMapper = objectMapper;
		this.webClient = webClient;
	}

	public OpenAiChatChain(final String promptTemplate, final OpenAiChatParameters parameters, final String apiKey,
			final String systemTemplate) {
		this(promptTemplate, parameters, apiKey, systemTemplate, createDefaultObjectMapper(), createDefaultWebClient());
	}

	public OpenAiChatChain(final String promptTemplate, final OpenAiChatParameters parameters, final String apiKey) {
		this(promptTemplate, parameters, apiKey, null);
	}

	protected ResponseSpec createResponseSpec(final OpenAiChatCompletionsRequest request, final WebClient webClient,
			final ObjectMapper objectMapper) {
		return this.webClient.post()
				.uri(UriComponentsBuilder.newInstance().scheme("https").host("api.openai.com")
						.path("/v1/chat/completions").build().toUri())
				.contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.body(BodyInserters.fromValue(requestToBody(request, objectMapper))).retrieve();
	}

	@Override
	public String run(final Map<String, String> input) {
		LOGGER.info("run {}", getClass().getSimpleName());
		return createResponseSpec(createRequest(input), webClient, objectMapper).bodyToMono(String.class)
				.map(responseBody -> bodyToResponse(responseBody, objectMapper))
				.map(OpenAiChatCompletionsResponse::getChoices).map(choices -> choices.get(0).getMessage())
				.map(OpenAiChatMessage::getContent).block();
	}

	private OpenAiChatCompletionsRequest createRequest(final Map<String, String> input) {
		final List<OpenAiChatMessage> messages = new LinkedList<>();
		if (systemTemplate != null) {
			messages.add(new OpenAiChatMessage("system", new StringSubstitutor(input).replace(systemTemplate)));
		}
		messages.add(new OpenAiChatMessage("user", new StringSubstitutor(input).replace(getPromptTemplate())));

		final OpenAiChatCompletionsRequest request = new OpenAiChatCompletionsRequest(messages);
		if (parameters != null) {
			request.copyFrom(parameters);
		}

		return request;
	}

	protected String requestToBody(final OpenAiChatCompletionsRequest request, final ObjectMapper objectMapper) {
		try {
			return objectMapper.writeValueAsString(request);
		} catch (final JsonProcessingException jsonProcessingException) {
			throw new IllegalStateException("error creating request body", jsonProcessingException);
		}
	}

	private OpenAiChatCompletionsResponse bodyToResponse(final String responseBody, final ObjectMapper objectMapper) {
		try {
			return objectMapper.readValue(responseBody, OpenAiChatCompletionsResponse.class);
		} catch (final JsonProcessingException jsonProcessingException) {
			throw new IllegalStateException("error deserializing responseBody " + responseBody,
					jsonProcessingException);
		}
	}

	protected final String getApiKey() {
		return apiKey;
	}

	public static ObjectMapper createDefaultObjectMapper() {
		return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	public static WebClient createDefaultWebClient() {
		return WebClient.create();
	}
}
