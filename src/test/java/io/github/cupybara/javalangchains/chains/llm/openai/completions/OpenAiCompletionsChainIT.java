package io.github.cupybara.javalangchains.chains.llm.openai.completions;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import io.github.cupybara.javalangchains.chains.Chain;

/**
 * Integration Tests for the {@link OpenAiCompletionsChain}
 */
class OpenAiCompletionsChainIT {

	private static final Logger LOGGER = LogManager.getLogger();

	@Test
	void testRun() {
		final OpenAiCompletionsParameters parameters = new OpenAiCompletionsParameters();
		parameters.setModel("text-davinci-003");

		final OpenAiCompletionsChain chain = new OpenAiCompletionsChain(
				"Hello, this is ${name}. What was my name again?", parameters, System.getenv("OPENAI_API_KEY"));

		final String name = "Manuel";
		final String result = chain.run(Collections.singletonMap("name", name));
		LOGGER.info(result);

		assertNotNull(result, "got no result from OpenAiCompletionsChain");
		assertTrue(result.contains(name), "The answer did not contain the name");
	}

	@Test
	void testChainedRun() {
		final OpenAiCompletionsParameters parameters = new OpenAiCompletionsParameters();
		parameters.setModel("text-davinci-003");

		final Chain<Map<String, String>, String> chain = new OpenAiCompletionsChain(
				"Hello, this is ${name}. What is your name?", parameters, System.getenv("OPENAI_API_KEY"))
				.chain(prev -> Collections.singletonMap("result", prev))
				.chain(new OpenAiCompletionsChain("What was the question for the following answer: ${result}",
						parameters, System.getenv("OPENAI_API_KEY")));

		final String result = chain.run(Collections.singletonMap("name", "Manuel"));
		LOGGER.info(result);

		assertNotNull(result, "got no result from chain");
	}
}
