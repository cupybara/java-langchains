package com.github.hakenadu.javalangchains.chains.base;

import java.util.stream.Stream;

import com.github.hakenadu.javalangchains.chains.Chain;

/**
 * utility {@link Chain} which wraps the output of the previous {@link Chain} in
 * a {@link Stream} for processing using {@link Stream} consuming {@link Chain
 * Chains}.
 */
public final class StreamWrappingChain<T> implements Chain<T, Stream<T>> {

	@Override
	public Stream<T> run(final T input) {
		return Stream.of(input);
	}
}
