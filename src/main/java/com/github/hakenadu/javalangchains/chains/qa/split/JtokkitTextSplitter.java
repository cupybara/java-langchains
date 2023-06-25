package com.github.hakenadu.javalangchains.chains.qa.split;

import com.knuddels.jtokkit.api.Encoding;

/**
 * This {@link TextSplitter} splits documents based on their token count. For
 * that purpose <a href="https://github.com/knuddelsgmbh/jtokkit">jtokkit</a> is
 * utilized.
 */
public final class JtokkitTextSplitter extends MaxLengthBasedTextSplitter {

	/**
	 * the {@link Encoding} used for token counting
	 */
	private final Encoding encoding;

	/**
	 * creates an instance of {@link JtokkitTextSplitter}
	 * 
	 * @param encoding     {@link #encoding}
	 * @param maxTokens    max amount of tokens for each chunk
	 * @param textStreamer the {@link TextStreamer} used for streaming the base text
	 */
	public JtokkitTextSplitter(final Encoding encoding, final int maxTokens, final TextStreamer textStreamer) {
		super(maxTokens, textStreamer);
		this.encoding = encoding;
	}

	/**
	 * creates an instance of {@link JtokkitTextSplitter} with sentence based text
	 * streaming
	 * 
	 * @param encoding  {@link #encoding}
	 * @param maxTokens max amount of tokens for each chunk
	 */
	public JtokkitTextSplitter(final Encoding encoding, final int maxTokens) {
		this(encoding, maxTokens, new TextStreamer());
	}

	@Override
	protected int getLength(final String textPart) {
		return encoding.countTokens(textPart);
	}
}
