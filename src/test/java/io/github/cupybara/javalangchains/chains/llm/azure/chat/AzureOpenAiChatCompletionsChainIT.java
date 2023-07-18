package io.github.cupybara.javalangchains.chains.llm.azure.chat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import io.github.cupybara.javalangchains.chains.Chain;
import io.github.cupybara.javalangchains.chains.llm.openai.chat.OpenAiChatCompletionsParameters;

/**
 * Integration Tests for the {@link AzureOpenAiChatCompletionsChain}
 */
class AzureOpenAiChatCompletionsChainIT {

	private static final Logger LOGGER = LogManager.getLogger();

	@Test
	void testRun() {
		final AzureOpenAiChatCompletionsChain chain = new AzureOpenAiChatCompletionsChain(
				"my-azure-resource-name", "gpt-35-turbo", "2023-05-15", 
				"Hello, this is ${name}. What was my name again?",
				new OpenAiChatCompletionsParameters(), System.getenv("AZURE_OPENAI_API_KEY"));

		final String name = "Manuel";
		final String result = chain.run(Collections.singletonMap("name", name));
		LOGGER.info(result);

		assertNotNull(result, "got no result from OpenAiChatCompletionsChain");
		assertTrue(result.contains(name), "The answer did not contain the name");
	}

	@Test
	void testChainedRun() {
		final OpenAiChatCompletionsParameters parameters = new OpenAiChatCompletionsParameters();

		final Chain<Map<String, String>, String> chain = new AzureOpenAiChatCompletionsChain(
					"my-azure-resource-name", "gpt-35-turbo", "2023-05-15",
					"Hello, this is ${name}. What was my name again?", 
					parameters, System.getenv("AZURE_OPENAI_API_KEY"))
				.chain(prev -> Collections.singletonMap("result", prev))
				.chain(new AzureOpenAiChatCompletionsChain(
						"my-azure-resource-name", "gpt-35-turbo", "2023-05-15",
						"What was the question for the following answer: ${result}", 
						parameters, System.getenv("AZURE_OPENAI_API_KEY")));

		final String result = chain.run(Collections.singletonMap("name", "Manuel"));
		LOGGER.info(result);

		assertNotNull(result, "got no result from chain");
	}
}
