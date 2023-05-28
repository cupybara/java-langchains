package com.github.hakenadu.javalangchain.links;

@FunctionalInterface
public interface ChainLink<I, O> {

	O run(I input);

	default <B> ChainLink<I, B> chain(final ChainLink<O, B> next) {
		return input -> next.run(run(input));
	}
}
