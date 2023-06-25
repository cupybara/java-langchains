package com.github.hakenadu.javalangchains.chains.qa.split;

import java.text.BreakIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * streams a text using a {@link BreakIterator}
 */
public class TextStreamer {

	/**
	 * creates the {@link BreakIterator} used for streaming
	 */
	private final Supplier<BreakIterator> breakIteratorSupplier;

	/**
	 * creates a {@link TextStreamer} using a custom {@link BreakIterator}
	 * 
	 * @param breakIteratorSupplier {@link #breakIteratorSupplier}
	 */
	public TextStreamer(final Supplier<BreakIterator> breakIteratorSupplier) {
		this.breakIteratorSupplier = breakIteratorSupplier;
	}

	/**
	 * creates a {@link TextStreamer} which streams sentences
	 */
	public TextStreamer() {
		this(BreakIterator::getSentenceInstance);
	}

	/**
	 * creates a stream of text partitions
	 * 
	 * @param text partitionized text
	 * @return {@link Stream} of text partitions
	 */
	public Stream<String> stream(final String text) {
		final BreakIterator breakIterator = breakIteratorSupplier.get();
		breakIterator.setText(text);

		final Iterator<String> breakIteratorAdapter = new Iterator<String>() {
			int start = breakIterator.first();
			int end = breakIterator.next();

			@Override
			public boolean hasNext() {
				return end != BreakIterator.DONE;
			}

			@Override
			public String next() {
				if (end == BreakIterator.DONE) {
					throw new NoSuchElementException("No more words");
				}

				final String textPartition = text.substring(start, end);
				start = end;
				end = breakIterator.next();
				return textPartition;
			}
		};

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(breakIteratorAdapter, Spliterator.ORDERED),
				false);
	}
}
