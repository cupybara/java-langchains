package com.github.hakenadu.javalangchain.chains;

@FunctionalInterface
public interface Chain<I, O> {

	O run(I input);

	default <B> Chain<I, B> chain(final Chain<O, B> next) {
		return input -> next.run(run(input));
	}
}
