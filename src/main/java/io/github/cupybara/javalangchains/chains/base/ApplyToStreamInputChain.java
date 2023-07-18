package io.github.cupybara.javalangchains.chains.base;

import java.util.stream.Stream;

import io.github.cupybara.javalangchains.chains.Chain;

/**
 * this chain applies another chain (which is passed as a constructor parameter)
 * to each item of the input stream.
 *
 * @param <I> the type of each item in the input stream
 * @param <O> the type of each item in the output stream
 */
public final class ApplyToStreamInputChain<I, O> implements Chain<Stream<I>, Stream<O>> {

	/**
	 * this chain is applied to each item of the input stream
	 */
	private final Chain<I, O> applyToStreamItemChain;

	/**
	 * @param applyToStreamItemChain {@link #applyToStreamItemChain}
	 */
	public ApplyToStreamInputChain(final Chain<I, O> applyToStreamItemChain) {
		this.applyToStreamItemChain = applyToStreamItemChain;
	}

	@Override
	public Stream<O> run(final Stream<I> input) {
		return input.map(this.applyToStreamItemChain::run);
	}
}
