package com.github.hakenadu.javalangchains.chains.llm.openai;

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
import com.github.hakenadu.javalangchains.chains.llm.LargeLanguageModelChain;

/**
 * {@link LargeLanguageModelChain} for usage with the OpenAI API
 */
public class OpenAiChatChain extends LargeLanguageModelChain {

	/**
	 * The template for the system role which contains placeholders in the form
	 * ${myPlaceholder} that are replaced for input documents before creating a
	 * request to a LLM.
	 */
	private final String systemTemplate;

	/**
	 * The {@link OpenAiChatParameters} allows to finetune requests to the OpenAI
	 * API
	 */
	private final OpenAiChatParameters parameters;

	/**
	 * The API-Key used for Authentication
	 */
	private final String apiKey;

	/**
	 * The {@link ObjectMapper} used for body serialization and deserialization
	 */
	private final ObjectMapper objectMapper;

	/**
	 * The {@link WebClient} used for executing requests to the OpenAI API
	 */
	private final WebClient webClient;

	/**
	 * @param promptTemplate {@link #getPromptTemplate()}
	 * @param parameters     {@link #parameters}r
	 * @param apiKey         {@link #apiKey}
	 * @param systemTemplate {@link #systemTemplate}
	 * @param objectMapper   {@link #objectMapper}
	 * @param webClient      {@link #webClient}
	 */
	public OpenAiChatChain(final String promptTemplate, final OpenAiChatParameters parameters, final String apiKey,
			final String systemTemplate, final ObjectMapper objectMapper, final WebClient webClient) {
		super(promptTemplate);
		this.parameters = parameters;
		this.apiKey = apiKey;
		this.systemTemplate = systemTemplate;
		this.objectMapper = objectMapper;
		this.webClient = webClient;
	}

	/**
	 * @param promptTemplate {@link #getPromptTemplate()}
	 * @param parameters     {@link #parameters}
	 * @param apiKey         {@link #apiKey}
	 * @param systemTemplate {@link #systemTemplate}s
	 */
	public OpenAiChatChain(final String promptTemplate, final OpenAiChatParameters parameters, final String apiKey,
			final String systemTemplate) {
		this(promptTemplate, parameters, apiKey, systemTemplate, createDefaultObjectMapper(), createDefaultWebClient());
	}

	/**
	 * @param promptTemplate {@link #getPromptTemplate()}
	 * @param parameters     {@link #parameters}
	 * @param apiKey         {@link #apiKey}
	 */
	public OpenAiChatChain(final String promptTemplate, final OpenAiChatParameters parameters, final String apiKey) {
		this(promptTemplate, parameters, apiKey, null);
	}

	/**
	 * executes the request to the OpenAI API. Protected so that it may be
	 * overridden for other OpenAI API Providers.
	 * 
	 * @param request      the {@link OpenAiChatCompletionsRequest}
	 * @param webClient    the {@link WebClient} to use for the request
	 * @param objectMapper the {@link ObjectMapper} used for body serialization
	 * @return the {@link ResponseSpec}
	 */
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

	/**
	 * Serializes the {@link OpenAiChatCompletionsRequest}
	 * 
	 * @param request      {@link OpenAiChatCompletionsRequest} to serialize
	 * @param objectMapper {@link ObjectMapper} used for serialization
	 * @return serialized {@link OpenAiChatCompletionsRequest}
	 */
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

	/**
	 * @return {@link #apiKey}p
	 */
	protected final String getApiKey() {
		return apiKey;
	}

	/**
	 * @return a default configured {@link ObjectMapper}
	 */
	public static ObjectMapper createDefaultObjectMapper() {
		return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	/**
	 * @return a default configured {@link WebClient}
	 */
	public static WebClient createDefaultWebClient() {
		return WebClient.create();
	}
}
