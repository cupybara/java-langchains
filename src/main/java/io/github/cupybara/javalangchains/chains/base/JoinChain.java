package io.github.cupybara.javalangchains.chains.base;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import io.github.cupybara.javalangchains.chains.Chain;

/**
 * This {@link Chain} is used to join multiple other chains. Their output is
 * provided as a {@link Stream} which will be passed as an input to subsequent
 * chains.
 * 
 * @param <I> Input type of joined chains
 * @param <O> Output type of joined chains
 */
public final class JoinChain<I, O> implements Chain<I, Stream<O>> {

	/**
	 * the list of joined {@link Chain Chains}
	 */
	private final List<Chain<I, O>> chains;

	/**
	 * if <code>true</code> the result stream will be a parallel one
	 */
	private final boolean parallel;

	/**
	 * @param parallel {@link #parallel}
	 * @param chains   {@link #chains}
	 */
	public JoinChain(final boolean parallel, final List<Chain<I, O>> chains) {
		this.parallel = parallel;
		this.chains = chains;
	}

	/**
	 * @param chains {@link #chains}
	 */
	public JoinChain(final List<Chain<I, O>> chains) {
		this(false, chains);
	}

	/**
	 * @param parallel {@link #parallel}
	 * @param chains   {@link #chains}
	 */
	@SafeVarargs
	public JoinChain(final boolean parallel, final Chain<I, O>... chains) {
		this(parallel, Arrays.asList(chains));
	}

	/**
	 * @param chains {@link #chains}
	 */
	@SafeVarargs
	public JoinChain(final Chain<I, O>... chains) {
		this(false, chains);
	}

	@Override
	public Stream<O> run(final I input) {
		final Stream<O> result = chains.stream().map(chain -> chain.run(input));
		if (parallel) {
			return result.parallel();
		}
		return result;
	}
}
