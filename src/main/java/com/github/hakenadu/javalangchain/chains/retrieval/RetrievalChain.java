package com.github.hakenadu.javalangchain.chains.retrieval;

import java.util.Collection;

import com.github.hakenadu.javalangchain.chains.Chain;

/**
 * {@link Chain} which is utilized for retrieving documents in a QA context
 */
public abstract class RetrievalChain implements Chain<String, Collection<Document>> {

	/**
	 * maximum count of retrieved documents
	 */
	private final int maxDocumentCount;

	protected RetrievalChain(final int maxDocumentCount) {
		this.maxDocumentCount = maxDocumentCount;
	}

	protected final int getMaxDocumentCount() {
		return this.maxDocumentCount;
	}
}
