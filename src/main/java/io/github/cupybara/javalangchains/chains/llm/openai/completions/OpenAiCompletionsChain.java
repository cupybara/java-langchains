package io.github.cupybara.javalangchains.chains.llm.openai.completions;

import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cupybara.javalangchains.chains.llm.openai.OpenAiChain;

/**
 * {@link OpenAiChain} for usage with the OpenAI /completions API
 */
public class OpenAiCompletionsChain
		extends OpenAiChain<OpenAiCompletionsParameters, OpenAiCompletionsRequest, OpenAiCompletionsResponse> {

	/**
	 * @param promptTemplate {@link #getPromptTemplate()}
	 * @param parameters     {@link #parameters}r
	 * @param apiKey         {@link #apiKey}
	 * @param objectMapper   {@link #objectMapper}
	 * @param webClient      {@link #webClient}
	 */
	public OpenAiCompletionsChain(final String promptTemplate, final OpenAiCompletionsParameters parameters,
			final String apiKey, final ObjectMapper objectMapper, final WebClient webClient) {
		super(promptTemplate, "/v1/completions", OpenAiCompletionsResponse.class, parameters, apiKey, objectMapper,
				webClient);
	}

	/**
	 * @param promptTemplate {@link #getPromptTemplate()}
	 * @param parameters     {@link #parameters}
	 * @param apiKey         {@link #apiKey}
	 */
	public OpenAiCompletionsChain(final String promptTemplate, final OpenAiCompletionsParameters parameters,
			final String apiKey) {
		this(promptTemplate, parameters, apiKey, createDefaultObjectMapper(), createDefaultWebClient());
	}

	@Override
	protected OpenAiCompletionsRequest createRequest(final Map<String, String> input) {
		return new OpenAiCompletionsRequest(new StringSubstitutor(input).replace(getPromptTemplate()));
	}

	@Override
	protected String createOutput(final OpenAiCompletionsResponse response) {
		return response.getChoices().get(0).getText();
	}
}
