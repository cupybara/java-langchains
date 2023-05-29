package com.github.hakenadu.javalangchain.chains.llm.azure;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hakenadu.javalangchain.chains.llm.openai.OpenAiChatChain;
import com.github.hakenadu.javalangchain.chains.llm.openai.OpenAiChatCompletionsRequest;
import com.github.hakenadu.javalangchain.chains.llm.openai.OpenAiChatParameters;

/**
 * {@link OpenAiChatChain} adopted for usage of Azure OpenAI Services
 */
public final class AzureOpenAiChatChain extends OpenAiChatChain {

	private final String resourceName;
	private final String deploymentName;
	private final String apiVersion;

	/**
	 * @param resourceName   {@link #resourceName}
	 * @param deploymentName {@link #deploymentName}
	 * @param apiVersion     {@link #apiVersion}
	 * @param promptTemplate The template which contains placeholders in the form
	 *                       ${myPlaceholder} that are replaced for input documents
	 *                       before creating a request to a LLM.
	 * @param parameters     The {@link OpenAiChatParameters} allows to finetune
	 *                       requests to the OpenAI API
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
	public AzureOpenAiChatChain(final String resourceName, final String deploymentName, final String apiVersion,
			final String promptTemplate, final OpenAiChatParameters parameters, final String apiKey,
			final String systemTemplate, final ObjectMapper objectMapper, final WebClient webClient) {
		super(promptTemplate, parameters, apiKey, systemTemplate, objectMapper, webClient);
		this.resourceName = resourceName;
		this.deploymentName = deploymentName;
		this.apiVersion = apiVersion;
	}

	/**
	 * @param resourceName   {@link #resourceName}
	 * @param deploymentName {@link #deploymentName}
	 * @param apiVersion     {@link #apiVersion}
	 * @param promptTemplate The template which contains placeholders in the form
	 *                       ${myPlaceholder} that are replaced for input documents
	 *                       before creating a request to a LLM.
	 * @param parameters     The {@link OpenAiChatParameters} allows to finetune
	 *                       requests to the OpenAI API
	 * @param apiKey         The API-Key used for Authentication (passed using the
	 *                       "api-key" Header)
	 * @param systemTemplate The template for the system role which contains
	 *                       placeholders in the form ${myPlaceholder} that are
	 *                       replaced for input documents before creating a request
	 */
	public AzureOpenAiChatChain(final String resourceName, final String deploymentName, final String apiVersion,
			final String promptTemplate, final OpenAiChatParameters parameters, final String apiKey,
			final String systemTemplate) {
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
	 * @param parameters     The {@link OpenAiChatParameters} allows to finetune
	 *                       requests to the OpenAI API
	 * @param apiKey         The API-Key used for Authentication (passed using the
	 *                       "api-key" Header)
	 */
	public AzureOpenAiChatChain(final String resourceName, final String deploymentName, final String apiVersion,
			final String promptTemplate, final OpenAiChatParameters parameters, final String apiKey) {
		this(resourceName, deploymentName, apiVersion, promptTemplate, parameters, apiKey, null);
	}

	@Override
	protected ResponseSpec createResponseSpec(final OpenAiChatCompletionsRequest request, final WebClient webClient,
			final ObjectMapper objectMapper) {
		return webClient.post()
				.uri(UriComponentsBuilder.newInstance().scheme("https").host(createRequestHostName())
						.queryParam("api-version", apiVersion).path(createRequestPath()).build().toUri())
				.contentType(MediaType.APPLICATION_JSON).header("api-key", getApiKey())
				.body(BodyInserters.fromValue(requestToBody(request, objectMapper))).retrieve();
	}

	private String createRequestHostName() {
		return String.format("%s.openai.azure.com", resourceName);
	}

	private String createRequestPath() {
		return String.format("/openai/deployments/%s/chat/completions", deploymentName);
	}
}
