package com.github.hakenadu.javalangchain.links.llm.openai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class OpenAiChatChainLinkTest {

	@Test
	void testRun() {
		final OpenAiChatParameters parameters = new OpenAiChatParameters();
		parameters.setModel("gpt-3.5-turbo");

		final OpenAiChatChainLink chainLink = new OpenAiChatChainLink("Hello, this is ${name}. What was my name again?",
				parameters);

		final String name = "Manuel";
		final String result = chainLink.run(Collections.singletonMap("name", name));
		assertNotNull(result, "got no result from OpenAiChatChainLink");
		assertTrue(result.contains(name), "The answer did not contain the name");
	}
}
