package com.github.hakenadu.javalangchain.chains.retrieval;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class AnswerWithSources {

	private final String answer;
	private final List<String> sources;

	public AnswerWithSources(final String answer, final List<String> sources) {
		this.answer = answer;
		this.sources = sources;
	}

	public AnswerWithSources(final String answer) {
		this(answer, Collections.emptyList());
	}

	public String getAnswer() {
		return answer;
	}

	public List<String> getSources() {
		return sources;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", getAnswer(), getSources().stream().collect(Collectors.joining(", ")));
	}
}
