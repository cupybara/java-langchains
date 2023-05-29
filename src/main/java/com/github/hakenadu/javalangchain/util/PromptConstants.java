package com.github.hakenadu.javalangchain.util;

/**
 * Utility Class which holds constants for prompt placeholders
 */
public final class PromptConstants {

	/**
	 * placeholder for the question in qa context
	 */
	public static final String QUESTION = "question";

	/**
	 * placeholder for text content in qa context
	 */
	public static final String CONTENT = "content";

	/**
	 * placeholder for sources in qa context
	 */
	public static final String SOURCE = "source";

	private PromptConstants() {
		// not instantiated
	}
}
