package com.github.hakenadu.javalangchains.chains.base;

import java.util.stream.Stream;

import com.github.hakenadu.javalangchains.chains.Chain;

public final class StreamUnwrappingChain<T> implements Chain<Stream<T>, T> {

	@Override
	public T run(final Stream<T> input) {
		return input.findAny().orElseThrow();
	}
}
