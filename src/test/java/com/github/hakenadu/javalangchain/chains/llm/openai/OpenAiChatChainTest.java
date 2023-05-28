package com.github.hakenadu.javalangchain.chains.llm.openai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchain.chains.Chain;

class OpenAiChatChainTest {

	private static final Logger LOGGER = LogManager.getLogger();

	@Test
	void testRun() {
		final OpenAiChatParameters parameters = new OpenAiChatParameters();
		parameters.setModel("gpt-3.5-turbo");

		final OpenAiChatChain chain = new OpenAiChatChain("Hello, this is ${name}. What was my name again?", parameters,
				System.getenv("OPENAI_API_KEY"));

		final String name = "Manuel";
		final String result = chain.run(Collections.singletonMap("name", name));
		LOGGER.info(result);

		assertNotNull(result, "got no result from OpenAiChatChainLink");
		assertTrue(result.contains(name), "The answer did not contain the name");
	}

	@Test
	void testChainedRun() {
		final OpenAiChatParameters parameters = new OpenAiChatParameters();
		parameters.setModel("gpt-3.5-turbo");

		final Chain<Map<String, String>, String> chain = new OpenAiChatChain(
				"Hello, this is ${name}. What is your name?", parameters, System.getenv("OPENAI_API_KEY"))
				.chain(prev -> Collections.singletonMap("result", prev))
				.chain(new OpenAiChatChain("What was the question for the following answer: ${result}", parameters,
						System.getenv("OPENAI_API_KEY")));

		final String result = chain.run(Collections.singletonMap("name", "Manuel"));
		LOGGER.info(result);

		assertNotNull(result, "got no result from chain");
	}
}
