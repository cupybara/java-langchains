package io.github.cupybara.javalangchains.chains.qa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * unit tests for the class {@link MapAnswerWithSourcesChain}
 */
class MapAnswerWithSourcesChainTest {

	@Test
	void testRunSources() {
		final AnswerWithSources answerWithSources = new MapAnswerWithSourcesChain()
				.run("This is my test content. Sources: source-1, source-2");
		assertNotNull(answerWithSources, "got no answer");
		assertEquals("This is my test content.", answerWithSources.getAnswer(), "wrong answer");
		assertEquals(2, answerWithSources.getSources().size(), "wrong sources count");
	}

	@Test
	void testRunSource() {
		final AnswerWithSources answerWithSources = new MapAnswerWithSourcesChain()
				.run("This is my test content. Source: source-1");
		assertNotNull(answerWithSources, "got no answer");
		assertEquals("This is my test content.", answerWithSources.getAnswer(), "wrong answer");
		assertEquals(1, answerWithSources.getSources().size(), "wrong sources count");
	}

	@Test
	void testRunNoSource() {
		final AnswerWithSources answerWithSources = new MapAnswerWithSourcesChain().run("This is my test content.");
		assertNotNull(answerWithSources, "got no answer");
		assertEquals("This is my test content.", answerWithSources.getAnswer(), "wrong answer");
		assertTrue(answerWithSources.getSources().isEmpty(), "got sources bot there are none");
	}

	@Test
	void testRunSourcesMultiline() {
		final AnswerWithSources answerWithSources = new MapAnswerWithSourcesChain()
				.run("This is my test content.\nSOURCES: source-1, source-2");
		assertNotNull(answerWithSources, "got no answer");
		assertEquals("This is my test content.", answerWithSources.getAnswer(), "wrong answer");
		assertEquals(2, answerWithSources.getSources().size(), "wrong sources count");
	}
}
