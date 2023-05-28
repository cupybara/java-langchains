package com.github.hakenadu.javalangchain.chains;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@FunctionalInterface
public interface Chain<I, O> {
	static Logger LOGGER = LogManager.getLogger(Chain.class);

	O run(I input);

	default <B> Chain<I, B> chain(final Chain<O, B> next) {
		return input -> {
			final O prevOutput = run(input);
			LOGGER.debug("output: {}", prevOutput);

			final B nextOutput = next.run(prevOutput);
			LOGGER.debug("next output: {}", nextOutput);

			return nextOutput;
		};
	}
}
