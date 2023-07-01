package com.github.hakenadu.javalangchains.chains.llm.openai.chat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchains.chains.Chain;

/**
 * Integration Tests for the {@link OpenAiChatCompletionsChain}
 */
class OpenAiChatCompletionsChainIT {

	private static final Logger LOGGER = LogManager.getLogger();

	@Test
	void testRun() {
		final OpenAiChatCompletionsParameters parameters = new OpenAiChatCompletionsParameters();
		parameters.setModel("gpt-3.5-turbo");
		parameters.setTemperature(0D);

		final OpenAiChatCompletionsChain chain = new OpenAiChatCompletionsChain(
				"Hello, this is ${name}. What was my name again?", parameters, System.getenv("OPENAI_API_KEY"),
				"You are a helpful assistant who answers questions to ${name}");

		final String name = "Manuel";
		final String result = chain.run(Collections.singletonMap("name", name));
		LOGGER.info(result);

		assertNotNull(result, "got no result from OpenAiChatCompletionsChain");
		assertTrue(result.contains(name), "The answer did not contain the name");
	}

	@Test
	void testChainedRun() {
		final OpenAiChatCompletionsParameters parameters = new OpenAiChatCompletionsParameters();
		parameters.setModel("gpt-3.5-turbo");

		final Chain<Map<String, String>, String> chain = new OpenAiChatCompletionsChain(
				"Hello, this is ${name}. What is your name?", parameters, System.getenv("OPENAI_API_KEY"))
				.chain(prev -> Collections.singletonMap("result", prev))
				.chain(new OpenAiChatCompletionsChain("What was the question for the following answer: ${result}",
						parameters, System.getenv("OPENAI_API_KEY")));

		final String result = chain.run(Collections.singletonMap("name", "Manuel"));
		LOGGER.info(result);

		assertNotNull(result, "got no result from chain");
	}
}
