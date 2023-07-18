package io.github.cupybara.javalangchains.chains.qa;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.cupybara.javalangchains.chains.Chain;

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

	/**
	 * this {@link Pattern} is used to retrieve sources from a qa result string
	 */
	private final Pattern retrieveSourcesPattern;

	/**
	 * @param retrieveSourcesPattern {@link #retrieveSourcesPattern}
	 */
	public MapAnswerWithSourcesChain(final Pattern retrieveSourcesPattern) {
		this.retrieveSourcesPattern = retrieveSourcesPattern;
	}

	/**
	 * @param retrieveSourcesRegex used to create the
	 *                             {@link #retrieveSourcesPattern}
	 */
	public MapAnswerWithSourcesChain(final String retrieveSourcesRegex) {
		this.retrieveSourcesPattern = Pattern.compile(retrieveSourcesRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	}

	/**
	 * creates an instance of {@link MapAnswerWithSourcesChain} with a default regex
	 * to retrieve sources
	 */
	public MapAnswerWithSourcesChain() {
		this("(.*?)(?:Source(?:s)?:\\s*)(.*)");
	}

	@Override
	public AnswerWithSources run(final String input) {
		final Matcher matcher = retrieveSourcesPattern.matcher(input);

		if (matcher.find()) {
			final String content = matcher.group(1).trim();
			final String sources = matcher.group(2).trim();
			return new AnswerWithSources(content,
					Arrays.stream(sources.split(",")).map(String::trim).distinct().collect(Collectors.toList()));
		} else {
			return new AnswerWithSources(input);
		}
	}
}
