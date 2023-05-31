package com.github.hakenadu.javalangchains.chains.llm.openai.completions;

import java.util.Set;

import com.github.hakenadu.javalangchains.chains.llm.openai.OpenAiParameters;

/**
 * Parameters for calling an OpenAI Completions Model
 * 
 * https://platform.openai.com/docs/api-reference/completions/create
 */
public class OpenAiCompletionsParameters extends OpenAiParameters<OpenAiCompletionsParameters> {

	/**
	 * <h2>From
	 * https://github.com/openai/openai-openapi/blob/master/openapi.yaml</h2>
	 * 
	 * Up to 4 sequences where the API will stop generating further tokens. The
	 * returned text will not contain the stop sequence.
	 */
	private Set<String> stop;

	/**
	 * Creates an instance of {@link OpenAiCompletionsParameters}
	 */
	public OpenAiCompletionsParameters() {
		super(OpenAiCompletionsParameters.class);
	}

	/**
	 * @return {@link #stop}
	 */
	public Set<String> getStop() {
		return stop;
	}

	/**
	 * @param stop {@link #stop}
	 */
	public void setStop(final Set<String> stop) {
		this.stop = stop;
	}

	/**
	 * @param stop {@link #stop}
	 * @return this
	 */
	public OpenAiCompletionsParameters stop(final Set<String> stop) {
		setStop(stop);
		return this;
	}
}
