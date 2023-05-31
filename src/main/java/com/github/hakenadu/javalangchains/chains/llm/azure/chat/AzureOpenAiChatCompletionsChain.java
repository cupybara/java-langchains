package com.github.hakenadu.javalangchains.chains.llm.azure.chat;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hakenadu.javalangchains.chains.llm.openai.chat.OpenAiChatCompletionsChain;
import com.github.hakenadu.javalangchains.chains.llm.openai.chat.OpenAiChatCompletionsParameters;
import com.github.hakenadu.javalangchains.chains.llm.openai.chat.OpenAiChatCompletionsRequest;

/**
 * {@link OpenAiChatCompletionsChain} adopted for usage of Azure OpenAI Services
 */
public final class AzureOpenAiChatCompletionsChain extends OpenAiChatCompletionsChain {

	private final URI requestUri;

	/**
	 * @param resourceName   Name of the azure resource
	 * @param deploymentName Name of the azure openai service deployment
	 * @param apiVersion     The target API Version
	 * @param promptTemplate The template which contains placeholders in the form
	 *                       ${myPlaceholder} that are replaced for input documents
	 *                       before creating a request to a LLM.
	 * @param parameters     The {@link OpenAiChatCompletionsParameters} allows to
	 *                       finetune requests to the OpenAI API
	 * @param apiKey         The API-Key used for Authentication (passed using the
	 *                       "api-key" Header)
	 * @param systemTemplate The template for the system role which contains
	 *                       placeholders in the form ${myPlaceholder} that are
	 *                       replaced for input documents before creating a request
	 *                       to a LLM.
	 * @param objectMapper   The {@link ObjectMapper} used for body serialization
	 *                       and deserialization
	 * @param webClient      The {@link WebClient} used for executing requests to
	 *                       the OpenAI API
	 */
	public AzureOpenAiChatCompletionsChain(final String resourceName, final String deploymentName,
			final String apiVersion, final String promptTemplate, final OpenAiChatCompletionsParameters parameters,
			final String apiKey, final String systemTemplate, final ObjectMapper objectMapper,
			final WebClient webClient) {
		super(promptTemplate, parameters, apiKey, systemTemplate, objectMapper, webClient);

		this.requestUri = UriComponentsBuilder.newInstance().scheme("https")
				.host(String.format("%s.openai.azure.com", resourceName)).queryParam("api-version", apiVersion)
				.path(String.format("/openai/deployments/%s/chat/completions", deploymentName)).build().toUri();
	}

	/**
	 * @param resourceName   {@link #resourceName}
	 * @param deploymentName {@link #deploymentName}
	 * @param apiVersion     {@link #apiVersion}
	 * @param promptTemplate The template which contains placeholders in the form
	 *                       ${myPlaceholder} that are replaced for input documents
	 *                       before creating a request to a LLM.
	 * @param parameters     The {@link OpenAiChatCompletionsParameters} allows to
	 *                       finetune requests to the OpenAI API
	 * @param apiKey         The API-Key used for Authentication (passed using the
	 *                       "api-key" Header)
	 * @param systemTemplate The template for the system role which contains
	 *                       placeholders in the form ${myPlaceholder} that are
	 *                       replaced for input documents before creating a request
	 */
	public AzureOpenAiChatCompletionsChain(final String resourceName, final String deploymentName,
			final String apiVersion, final String promptTemplate, final OpenAiChatCompletionsParameters parameters,
			final String apiKey, final String systemTemplate) {
		this(resourceName, deploymentName, apiVersion, promptTemplate, parameters, apiKey, systemTemplate,
				createDefaultObjectMapper(), createDefaultWebClient());
	}

	/**
	 * @param resourceName   {@link #resourceName}
	 * @param deploymentName {@link #deploymentName}
	 * @param apiVersion     {@link #apiVersion}
	 * @param promptTemplate The template which contains placeholders in the form
	 *                       ${myPlaceholder} that are replaced for input documents
	 *                       before creating a request to a LLM.
	 * @param parameters     The {@link OpenAiChatCompletionsParameters} allows to
	 *                       finetune requests to the OpenAI API
	 * @param apiKey         The API-Key used for Authentication (passed using the
	 *                       "api-key" Header)
	 */
	public AzureOpenAiChatCompletionsChain(final String resourceName, final String deploymentName,
			final String apiVersion, final String promptTemplate, final OpenAiChatCompletionsParameters parameters,
			final String apiKey) {
		this(resourceName, deploymentName, apiVersion, promptTemplate, parameters, apiKey, null);
	}

	@Override
	protected ResponseSpec createResponseSpec(final OpenAiChatCompletionsRequest request, final WebClient webClient,
			final ObjectMapper objectMapper) {
		return webClient.post().uri(requestUri).contentType(MediaType.APPLICATION_JSON).header("api-key", getApiKey())
				.body(BodyInserters.fromValue(requestToBody(request, objectMapper))).retrieve();
	}
}
