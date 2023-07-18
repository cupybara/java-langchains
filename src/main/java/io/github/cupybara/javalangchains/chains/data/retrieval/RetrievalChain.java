package io.github.cupybara.javalangchains.chains.data.retrieval;

import java.util.Map;
import java.util.stream.Stream;

import io.github.cupybara.javalangchains.chains.Chain;

/**
 * {@link Chain} which is utilized for retrieving documents in a QA context
 */
public abstract class RetrievalChain implements Chain<String, Stream<Map<String, String>>> {

	/**
	 * maximum count of retrieved documents
	 */
	private final int maxDocumentCount;

	/**
	 * @param maxDocumentCount {@link #maxDocumentCount}
	 */
	protected RetrievalChain(final int maxDocumentCount) {
		this.maxDocumentCount = maxDocumentCount;
	}

	/**
	 * @return {@link #maxDocumentCount}
	 */
	protected final int getMaxDocumentCount() {
		return this.maxDocumentCount;
	}
}
