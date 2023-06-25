package com.github.hakenadu.javalangchains.chains.qa.split;

import java.util.List;

/**
 * Implementations are used by the {@link SplitDocumentsChain}. A
 * {@link TextSplitter} takes an input text and creates a {@link List} with one
 * or more result strings based on the original text.
 */
@FunctionalInterface
public interface TextSplitter {

	/**
	 * Splits a text into one or more subtexts
	 * 
	 * @param text text to split
	 * @return {@link List} with text partitions
	 */
	List<String> split(String text);
}
