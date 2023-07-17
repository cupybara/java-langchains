package com.github.hakenadu.javalangchains.chains;

/**
 * Basic interface for all modular components in this repository. A
 * {@link Chain} accepts an input of type *I* and provides an output of type
 * *O*. Using the method {@link #chain(Chain)} passing another {@link Chain}, a
 * new {@link Chain} can be created which accepts the original chain's input and
 * provided the new chain's output.
 *
 * @param <I> the chain input type
 * @param <O> the chain output type
 */
@FunctionalInterface
public interface Chain<I, O> {

	/**
	 * Execute this {@link Chain}
	 * 
	 * @param input this chain's input
	 * @return this chain's output
	 */
	O run(I input);

	/**
	 * create a new {@link Chain} connecting this instance with another passed one.
	 * 
	 * @param <B>  type of the next chain's output
	 * @param next the next chain which is attached to this instance
	 * @return a new {@link Chain} consisting of the original {@link Chain} and the
	 *         passed one
	 */
	default <B> ChainLink<I, O, B> chain(final Chain<O, B> next) {
		return new ChainLink<>(this, next);
	}
}
