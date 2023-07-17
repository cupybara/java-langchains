package com.github.hakenadu.javalangchains.chains.base.logging;

import java.util.function.Consumer;
import java.util.function.Function;

import com.github.hakenadu.javalangchains.chains.Chain;

/**
 * this chain is used to log an input instance
 *
 * @param <I> the static input type
 */
public class LoggingChain<I> implements Chain<I, I> {

	private final String logPrefix;
	private final Consumer<String> logConsumer;
	private final Function<I, String> inputSerializer;

	/**
	 * @param logPrefix       {@link #logPrefix}
	 * @param logConsumer     {@link #logConsumer}
	 * @param inputSerializer {@link #inputSerializer}
	 */
	public LoggingChain(final String logPrefix, final Consumer<String> logConsumer,
			final Function<I, String> inputSerializer) {
		this.logPrefix = logPrefix;
		this.inputSerializer = inputSerializer;
		this.logConsumer = logConsumer;
	}

	/**
	 * @param logPrefix   {@link #logPrefix}
	 * @param logConsumer {@link #logConsumer}
	 */
	public LoggingChain(final String logPrefix, final Consumer<String> logConsumer) {
		this(logPrefix, logConsumer, String::valueOf);
	}

	/**
	 * @param logPrefix       {@link #logPrefix}
	 * @param inputSerializer {@link #inputSerializer}
	 */
	public LoggingChain(final String logPrefix, final Function<I, String> inputSerializer) {
		this(logPrefix, System.out::println, inputSerializer);
	}

	/**
	 * @param logPrefix {@link #logPrefix}
	 */
	public LoggingChain(final String logPrefix) {
		this(logPrefix, System.out::println, String::valueOf);
	}

	/**
	 * creates an instance of the {@link LoggingChain}
	 */
	public LoggingChain() {
		this("");
	}

	@Override
	public I run(final I input) {
		final String serializedInput = inputSerializer.apply(input);
		logConsumer.accept(String.format("%s%s", this.logPrefix, serializedInput));
		return input;
	}

	/**
	 * @param title the title for the logPrefix
	 * @return title with delimiting lines
	 */
	public static String defaultLogPrefix(final String title) {
		return "\n========================================================================================================================================================\n"
				+ title
				+ "\n========================================================================================================================================================\n";
	}
}
