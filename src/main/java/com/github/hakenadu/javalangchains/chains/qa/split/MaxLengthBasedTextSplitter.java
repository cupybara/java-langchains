package com.github.hakenadu.javalangchains.chains.qa.split;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * abstract base class for {@link TextSplitter} implementations that use a max
 * length for deciding when to split
 */
public abstract class MaxLengthBasedTextSplitter implements TextSplitter {

	/**
	 * max length for each chunk in the split
	 */
	private final int maxLength;

	/**
	 * the {@link TextStreamer} used for streaming the base text (sentence by
	 * sentence is the default)
	 */
	private final TextStreamer textStreamer;

	/**
	 * @param maxLength    {@link #maxLength}
	 * @param textStreamer {@link #textStreamer}
	 */
	protected MaxLengthBasedTextSplitter(final int maxLength, final TextStreamer textStreamer) {
		this.maxLength = maxLength;
		this.textStreamer = textStreamer;
	}

	/**
	 * creates a {@link MaxLengthBasedTextSplitter} using sentence wise text
	 * streaming
	 * 
	 * @param maxLength {@link #maxLength}
	 */
	protected MaxLengthBasedTextSplitter(final int maxLength) {
		this(maxLength, new TextStreamer());
	}

	/**
	 * provide the length value for a text part
	 * 
	 * @param textPart the text part which needs to be measured
	 * @return the length for the passed textPart
	 */
	protected abstract int getLength(String textPart);

	@Override
	public final List<String> split(final String text) {
		final List<String> split = new LinkedList<>();

		final AtomicReference<String> partition = new AtomicReference<>("");

		this.textStreamer.stream(text).forEach(textPart -> {
			final String newPartition = partition.get() + textPart;

			final int newPartitionLength = getLength(newPartition);
			if (newPartitionLength > maxLength) {

				// the current textPart must be part of the next chunk
				if (textPart.length() == newPartition.length()) {
					throw new IllegalStateException(
							"Text partition " + textPart + " is too long. Try to use another TextStreamer.");
				}
				split.add(partition.get());
				partition.set(textPart);

			} else if (newPartitionLength == maxLength) {

				// the current textPart is part of the current chunk but max length is reached
				split.add(newPartition);
				partition.set("");

			} else {
				// the current textPart is part of the current chunk
				partition.set(newPartition);
			}
		});

		final String trailingText = partition.get();
		if (trailingText.length() > 0) {
			split.add(trailingText);
		}

		return split;
	}

}
