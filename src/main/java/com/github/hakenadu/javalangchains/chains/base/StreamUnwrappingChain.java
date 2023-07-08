package com.github.hakenadu.javalangchains.chains.base;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import com.github.hakenadu.javalangchains.chains.Chain;

/**
 * a utility chain which is used to retrieve the element from a singleton stream
 *
 * @param <T> Type of the element in the {@link Stream}
 */
public final class StreamUnwrappingChain<T> implements Chain<Stream<T>, T> {

	@Override
	public T run(final Stream<T> input) {
		return input.findAny().orElseThrow(NoSuchElementException::new);
	}
}
