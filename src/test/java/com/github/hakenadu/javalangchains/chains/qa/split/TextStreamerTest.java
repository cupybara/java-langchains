package com.github.hakenadu.javalangchains.chains.qa.split;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Unit Tests for the {@link TextStreamer}
 */
class TextStreamerTest {

	private static final String TEXT_TO_SPLIT = "Hi there. This is an example text\nused for unit testing.";

	/**
	 * Tests the no args constructor version of the {@link TextStreamer}
	 */
	@Test
	void testStreamSentences() {
		final List<String> split = new TextStreamer() // no args constructor => sentences
				.stream(TEXT_TO_SPLIT).collect(Collectors.toList());
		assertNotNull(split, "got null result");
		assertEquals(2, split.size(), "wrong result count (2 sentences)");
		assertEquals("Hi there. ", split.get(0), "first sentence is wrong");
		assertEquals("This is an example text\nused for unit testing.", split.get(1), "second sentence is wrong");
	}
}
