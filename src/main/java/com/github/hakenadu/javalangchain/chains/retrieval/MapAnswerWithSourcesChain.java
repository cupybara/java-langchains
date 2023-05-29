package com.github.hakenadu.javalangchain.chains.retrieval;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.hakenadu.javalangchain.chains.Chain;

/**
 * Splits answers with sources from a QA chain.
 * 
 * <h2>Examples</h2>
 * 
 * <pre>
 *  I don't know who James Anderson is.
 *  SOURCES:
 * </pre>
 * 
 * <pre>
 *  There are two different John Does mentioned, one is a scientist and humanitarian, and the other is a traveler and author of a travel memoir. It is not clear if they are the same person or two different people with the same name.
 *  SOURCES: 1, 3
 * </pre>
 */
public class MapAnswerWithSourcesChain implements Chain<String, AnswerWithSources> {

	@Override
	public AnswerWithSources run(final String input) {
		final String[] answerAndSources = input.split("SOURCES:");
		if (answerAndSources.length == 2) {
			return new AnswerWithSources(answerAndSources[0].trim(),
					Arrays.stream(answerAndSources[1].split(",")).map(String::trim).collect(Collectors.toList()));
		}
		return new AnswerWithSources(answerAndSources[0].trim());
	}
}
