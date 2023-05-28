package com.github.hakenadu.javalangchain.chains.llm.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for calling an OpenAI Chat Model
 * 
 * @see https://platform.openai.com/docs/api-reference/chat/create
 */
public class OpenAiChatParameters {

	/**
	 * <h1>From
	 * https://github.com/openai/openai-openapi/blob/master/openapi.yaml</h1>
	 * 
	 * ID of the model to use. Currently, only `gpt-3.5-turbo` and
	 * `gpt-3.5-turbo-0301` are supported.
	 */
	private String model;

	/**
	 * <h1>From
	 * https://github.com/openai/openai-openapi/blob/master/openapi.yaml</h1>
	 * 
	 * What sampling temperature to use, between 0 and 2. Higher values like 0.8
	 * will make the output more random, while lower values like 0.2 will make it
	 * more focused and deterministic.
	 * 
	 * We generally recommend altering this or `top_p` but not both.
	 */
	@JsonProperty
	private Integer temperature;

	/**
	 * <h1>From
	 * https://github.com/openai/openai-openapi/blob/master/openapi.yaml</h1>
	 * 
	 * An alternative to sampling with temperature, called nucleus sampling, where
	 * the model considers the results of the tokens with top_p probability mass. So
	 * 0.1 means only the tokens comprising the top 10% probability mass are
	 * considered.
	 * 
	 * We generally recommend altering this or `temperature` but not both.
	 */
	@JsonProperty("top_p")
	private Double topP;

	/**
	 * <h1>From
	 * https://github.com/openai/openai-openapi/blob/master/openapi.yaml</h1>
	 * 
	 * How many completions to generate for each prompt.
	 **
	 * Note:** Because this parameter generates many completions, it can quickly
	 * consume your token quota. Use carefully and ensure that you have reasonable
	 * settings for `max_tokens` and `stop`.
	 */
	@JsonProperty
	private Integer n;

	public String getModel() {
		return model;
	}

	public void setModel(final String model) {
		this.model = model;
	}

	public Integer getTemperature() {
		return temperature;
	}

	public void setTemperature(final Integer temperature) {
		this.temperature = temperature;
	}

	public Double getTopP() {
		return topP;
	}

	public void setTopP(final Double topP) {
		this.topP = topP;
	}

	public Integer getN() {
		return n;
	}

	public void setN(final Integer n) {
		this.n = n;
	}

	public void copyFrom(final OpenAiChatParameters parameters) {
		this.setModel(parameters.getModel());
		this.setN(parameters.getN());
		this.setTemperature(parameters.getTemperature());
		this.setN(parameters.getN());
	}
}
