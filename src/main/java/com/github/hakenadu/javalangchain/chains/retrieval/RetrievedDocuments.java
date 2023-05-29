package com.github.hakenadu.javalangchain.chains.retrieval;

import java.util.Map;
import java.util.stream.Stream;

public final class RetrievedDocuments {

	private final String question;
	private final Stream<Map<String, String>> documents;

	public RetrievedDocuments(final String question, final Stream<Map<String, String>> documents) {
		super();
		this.question = question;
		this.documents = documents;
	}

	public String getQuestion() {
		return question;
	}

	public Stream<Map<String, String>> getDocuments() {
		return documents;
	}
}
