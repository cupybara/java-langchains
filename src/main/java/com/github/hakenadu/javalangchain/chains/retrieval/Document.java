package com.github.hakenadu.javalangchain.chains.retrieval;

public class Document {

	private final String id;
	private final String content;

	public Document(final String id, final String content) {
		this.id = id;
		this.content = content;
	}

	public final String getId() {
		return id;
	}

	public final String getContent() {
		return content;
	}
}
