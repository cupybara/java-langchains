package com.github.hakenadu.javalangchains.chains.base;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.github.hakenadu.javalangchains.chains.Chain;

public final class JoinChain<I, O> implements Chain<I, Stream<O>> {

	private final List<Chain<I, O>> chains;
	private final boolean parallel;

	public JoinChain(final boolean parallel, final List<Chain<I, O>> chains) {
		this.parallel = parallel;
		this.chains = chains;
	}

	public JoinChain(final List<Chain<I, O>> chains) {
		this(false, chains);
	}

	@SafeVarargs
	public JoinChain(final boolean parallel, final Chain<I, O>... chains) {
		this(parallel, Arrays.asList(chains));
	}

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
