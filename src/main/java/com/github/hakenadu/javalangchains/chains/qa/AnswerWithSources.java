package com.github.hakenadu.javalangchains.chains.qa;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class for QA output with sources
 */
public final class AnswerWithSources {

	private final String answer;
	private final List<String> sources;

	/**
	 * Creates an instance of {@link AnswerWithSources}
	 * 
	 * @param answer  {@link #answer}
	 * @param sources {@link #sources}
	 */
	public AnswerWithSources(final String answer, final List<String> sources) {
		this.answer = answer;
		this.sources = sources;
	}

	/**
	 * Creates an instance of {@link AnswerWithSources}
	 * 
	 * @param answer {@link #answer}
	 */
	public AnswerWithSources(final String answer) {
		this(answer, Collections.emptyList());
	}

	/**
	 * @return {@link #answer}
	 */
	public String getAnswer() {
		return answer;
	}

	/**
	 * @return {@link #sources}
	 */
	public List<String> getSources() {
		return sources;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", getAnswer(), getSources().stream().collect(Collectors.joining(", ")));
	}
}
