package io.github.cupybara.javalangchains.chains.llm.azure.completions;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cupybara.javalangchains.chains.llm.openai.chat.OpenAiChatCompletionsParameters;
import io.github.cupybara.javalangchains.chains.llm.openai.completions.OpenAiCompletionsChain;
import io.github.cupybara.javalangchains.chains.llm.openai.completions.OpenAiCompletionsParameters;
import io.github.cupybara.javalangchains.chains.llm.openai.completions.OpenAiCompletionsRequest;

/**
 * {@link OpenAiCompletionsChain} adopted for usage of Azure OpenAI Services
 */
public final class AzureOpenAiCompletionsChain extends OpenAiCompletionsChain {

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
	 * @param objectMapper   The {@link ObjectMapper} used for body serialization
	 *                       and deserialization
	 * @param webClient      The {@link WebClient} used for executing requests to
	 *                       the OpenAI API
	 */
	public AzureOpenAiCompletionsChain(final String resourceName, final String deploymentName, final String apiVersion,
			final String promptTemplate, final OpenAiCompletionsParameters parameters, final String apiKey,
			final ObjectMapper objectMapper, final WebClient webClient) {
		super(promptTemplate, parameters, apiKey, objectMapper, webClient);

		if (parameters.getModel() != null) {
			throw new IllegalArgumentException(
					"the model parameter cannot be used for the Azure OpenAI Services, it is passed via deploymentName instead");
		}

		this.requestUri = UriComponentsBuilder.newInstance().scheme("https")
				.host(String.format("%s.openai.azure.com", resourceName)).queryParam("api-version", apiVersion)
				.path(String.format("/openai/deployments/%s/completions", deploymentName)).build().toUri();
	}

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
	 */
	public AzureOpenAiCompletionsChain(final String resourceName, final String deploymentName, final String apiVersion,
			final String promptTemplate, final OpenAiCompletionsParameters parameters, final String apiKey) {
		this(resourceName, deploymentName, apiVersion, promptTemplate, parameters, apiKey, createDefaultObjectMapper(),
				createDefaultWebClient());
	}

	@Override
	protected ResponseSpec createResponseSpec(final OpenAiCompletionsRequest request, final WebClient webClient,
			final ObjectMapper objectMapper) {
		return webClient.post().uri(requestUri).contentType(MediaType.APPLICATION_JSON).header("api-key", getApiKey())
				.body(BodyInserters.fromValue(requestToBody(request, objectMapper))).retrieve();
	}
}
