package io.github.cupybara.javalangchains.chains.llm.openai.completions;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import io.github.cupybara.javalangchains.chains.llm.openai.OpenAiResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for the response body of an OpenAI /completions request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OpenAiCompletionsResponse extends OpenAiResponse<OpenAiCompletionsChoice> {

	/**
	 * @param choices {@link #getChoices()}
	 */
	@JsonCreator(mode = Mode.PROPERTIES)
	public OpenAiCompletionsResponse(final @JsonProperty("choices") List<OpenAiCompletionsChoice> choices) {
		super(choices);
	}
}
