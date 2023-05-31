package com.github.hakenadu.javalangchains.chains.llm.openai;

import java.util.Map;

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
 * {@link LargeLanguageModelChain} for usage with the OpenAI /completions API
 *
 * @param <P> the static parameter type
 * @param <I> the static request type
 * @param <O> the static response type
 */
public abstract class OpenAiChain<P extends OpenAiParameters<P>, I extends P, O extends OpenAiResponse<?>>
		extends LargeLanguageModelChain {

	/**
	 * The request path (/v1/chat/completions or /v1/completions)
	 */
	private final String requestPath;

	/**
	 * The response type class
	 */
	private final Class<O> responseClass;

	/**
	 * The {@link OpenAiParameters} allows to tune requests to the OpenAI API
	 */
	private final P parameters;

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
	 * @param requestPath    {@link #requestPath}
	 * @param responseClass  {@link #responseClass}
	 * @param parameters     {@link #parameters}r
	 * @param apiKey         {@link #apiKey}
	 * @param objectMapper   {@link #objectMapper}
	 * @param webClient      {@link #webClient}
	 */
	protected OpenAiChain(final String promptTemplate, final String requestPath, final Class<O> responseClass,
			final P parameters, final String apiKey, final ObjectMapper objectMapper, final WebClient webClient) {
		super(promptTemplate);
		this.requestPath = requestPath;
		this.responseClass = responseClass;
		this.parameters = parameters;
		this.apiKey = apiKey;
		this.objectMapper = objectMapper;
		this.webClient = webClient;
	}

	/**
	 * @param promptTemplate {@link #getPromptTemplate()}
	 * @param requestPath    {@link #requestPath}
	 * @param responseClass  {@link #responseClass}
	 * @param parameters     {@link #parameters}
	 * @param apiKey         {@link #apiKey}
	 */
	protected OpenAiChain(final String promptTemplate, final String requestPath, final Class<O> responseClass,
			final P parameters, final String apiKey) {
		this(promptTemplate, requestPath, responseClass, parameters, apiKey, createDefaultObjectMapper(),
				createDefaultWebClient());
	}

	/**
	 * creates the request entity from the current document
	 * 
	 * @param input the current document
	 * @return the request entity
	 */
	protected abstract I createRequest(final Map<String, String> input);

	/**
	 * creates the chain output from the response entity
	 * 
	 * @param response the response entity
	 * @return this chain's output
	 */
	protected abstract String createOutput(O response);

	@Override
	public String run(final Map<String, String> input) {
		LOGGER.info("run {}", getClass().getSimpleName());

		final I request = createRequest(input);
		if (parameters != null) {
			request.copyFrom(parameters);
		}

		return createResponseSpec(request, webClient, objectMapper).bodyToMono(String.class)
				.map(responseBody -> bodyToResponse(responseBody, objectMapper)).map(this::createOutput).block();
	}

	/**
	 * executes the request to the OpenAI API. Protected so that it may be
	 * overridden for other OpenAI API Providers.
	 * 
	 * @param request      the request entity
	 * @param webClient    the {@link WebClient} to use for the request
	 * @param objectMapper the {@link ObjectMapper} used for body serialization
	 * @return the {@link ResponseSpec}
	 */
	protected ResponseSpec createResponseSpec(final I request, final WebClient webClient,
			final ObjectMapper objectMapper) {
		return this.webClient.post()
				.uri(UriComponentsBuilder.newInstance().scheme("https").host("api.openai.com").path(requestPath).build()
						.toUri())
				.contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.body(BodyInserters.fromValue(requestToBody(request, objectMapper))).retrieve();
	}

	/**
	 * Serializes the request entity
	 * 
	 * @param request      the request entity to serialize
	 * @param objectMapper {@link ObjectMapper} used for serialization
	 * @return serialized the serialized request body
	 */
	protected String requestToBody(final I request, final ObjectMapper objectMapper) {
		try {
			return objectMapper.writeValueAsString(request);
		} catch (final JsonProcessingException jsonProcessingException) {
			throw new IllegalStateException("error creating request body", jsonProcessingException);
		}
	}

	private O bodyToResponse(final String responseBody, final ObjectMapper objectMapper) {
		try {
			return objectMapper.readValue(responseBody, this.responseClass);
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
