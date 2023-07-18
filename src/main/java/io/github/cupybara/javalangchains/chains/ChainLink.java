package io.github.cupybara.javalangchains.chains;

/**
 * A Link Between Worlds ;-)
 *
 * @param <I> type of the input chain's input
 * @param <M> type of the input chain's output and the output chain's input
 * @param <O> type of the output chain's output
 */
public final class ChainLink<I, M, O> implements Chain<I, O> {

	private final Chain<I, M> inputChain;
	private final Chain<M, O> outputChain;

	/**
	 * @param inputChain  {@link #inputChain}
	 * @param outputChain {@link #outputChain}
	 */
	ChainLink(final Chain<I, M> inputChain, final Chain<M, O> outputChain) {
		this.inputChain = inputChain;
		this.outputChain = outputChain;
	}

	@Override
	public O run(final I input) {
		final M intermediateOutput = inputChain.run(input);

		final O output = outputChain.run(intermediateOutput);

		return output;
	}

	/**
	 * @return true if this {@link ChainLink} is the first one of the whole chain
	 */
	public boolean isHead() {
		return !(inputChain instanceof ChainLink);
	}

	/**
	 * @return true if this {@link ChainLink} is the final one of the whole chain
	 */
	public boolean isTail() {
		return !(outputChain instanceof ChainLink);
	}

	/**
	 * @return {@link #inputChain}
	 */
	public Chain<I, M> getInputChain() {
		return inputChain;
	}

	/**
	 * @return {@link #outputChain}
	 */
	public Chain<M, O> getOutputChain() {
		return outputChain;
	}
}
