package com.github.hakenadu.javalangchains.chains.llm.openai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains the intersection of parameters for the /chat/completions and
 * /completions api
 *
 * @param <T> The type of this Parameter Class for typed fluent api return
 *            values
 */
public abstract class OpenAiParameters<T extends OpenAiParameters<T>> {

	/**
	 * The base type for correctly typed fluent api return values
	 */
	@JsonIgnore
	private final Class<T> typeClass;

	/**
	 * <h2>From
	 * https://github.com/openai/openai-openapi/blob/master/openapi.yaml</h2>
	 * 
	 * The maximum number of tokens allowed for the generated answer. By default,
	 * the number of tokens the model can return will be (4096 - prompt tokens).
	 */
	@JsonProperty("max_tokens")
	private Integer maxTokens;

	/**
	 * <h2>From
	 * https://github.com/openai/openai-openapi/blob/master/openapi.yaml</h2>
	 * 
	 * ID of the model to use. Currently, only `gpt-3.5-turbo` and
	 * `gpt-3.5-turbo-0301` are supported.
	 */
	private String model;

	/**
	 * <h2>From
	 * https://github.com/openai/openai-openapi/blob/master/openapi.yaml</h2>
	 * 
	 * How many completions to generate for each prompt.
	 **
	 * Note:** Because this parameter generates many completions, it can quickly
	 * consume your token quota. Use carefully and ensure that you have reasonable
	 * settings for `max_tokens` and `stop`.
	 */
	@JsonProperty
	private Integer n;

	/**
	 * <h2>From
	 * https://github.com/openai/openai-openapi/blob/master/openapi.yaml</h2>
	 * 
	 * What sampling temperature to use, between 0 and 2. Higher values like 0.8
	 * will make the output more random, while lower values like 0.2 will make it
	 * more focused and deterministic.
	 * 
	 * We generally recommend altering this or `top_p` but not both.
	 */
	@JsonProperty
	private Double temperature;

	/**
	 * @param typeClass {@link #typeClass}
	 */
	protected OpenAiParameters(final Class<T> typeClass) {
		this.typeClass = typeClass;
	}

	/**
	 * @return {@link #maxTokens}
	 */
	public Integer getMaxTokens() {
		return maxTokens;
	}

	/**
	 * @param maxTokens {@link #maxTokens}
	 */
	public void setMaxTokens(final Integer maxTokens) {
		this.maxTokens = maxTokens;
	}

	/**
	 * @param maxTokens {@link #maxTokens}
	 * @return this
	 */
	public T maxTokens(final Integer maxTokens) {
		setMaxTokens(maxTokens);
		return this.typeClass.cast(this);
	}

	/**
	 * @return {@link #model}
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @param model {@link #model}
	 */
	public void setModel(final String model) {
		this.model = model;
	}

	/**
	 * @param model {@link #model}
	 * @return this
	 */
	public T model(final String model) {
		setModel(model);
		return this.typeClass.cast(this);
	}

	/**
	 * @return {@link #n}
	 */
	public Integer getN() {
		return n;
	}

	/**
	 * @param n {@link #n}
	 */
	public void setN(final Integer n) {
		this.n = n;
	}

	/**
	 * @param n {@link #n}
	 * @return this
	 */
	public T n(final Integer n) {
		setN(n);
		return this.typeClass.cast(this);
	}

	/**
	 * @return {@link #temperature}
	 */
	public Double getTemperature() {
		return temperature;
	}

	/**
	 * @param temperature {@link #temperature}
	 */
	public void setTemperature(final Double temperature) {
		this.temperature = temperature;
	}

	/**
	 * @param temperature {@link #temperature}
	 * @return this
	 */
	public T temperature(final Double temperature) {
		this.setTemperature(temperature);
		return this.typeClass.cast(this);
	}

	/**
	 * copies parameter values from another instance of {@link OpenAiParameters}
	 * 
	 * @param parameters the source {@link OpenAiParameters}
	 */
	public void copyFrom(final T parameters) {
		this.setMaxTokens(parameters.getMaxTokens());
		this.setModel(parameters.getModel());
		this.setN(parameters.getN());
		this.setTemperature(parameters.getTemperature());
	}
}
