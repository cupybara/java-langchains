package com.github.hakenadu.javalangchain.links.llm.openai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchain.chains.Chain;
import com.github.hakenadu.javalangchain.chains.llm.openai.OpenAiChatChain;
import com.github.hakenadu.javalangchain.chains.llm.openai.OpenAiChatParameters;

class OpenAiChatChainTest {

	@Test
	void testRun() {
		final OpenAiChatParameters parameters = new OpenAiChatParameters();
		parameters.setModel("gpt-3.5-turbo");

		final OpenAiChatChain chain = new OpenAiChatChain("Hello, this is ${name}. What was my name again?",
				parameters);

		final String name = "Manuel";
		final String result = chain.run(Collections.singletonMap("name", name));
		assertNotNull(result, "got no result from OpenAiChatChainLink");
		assertTrue(result.contains(name), "The answer did not contain the name");
	}

	@Test
	void testChainedRun() {
		final OpenAiChatParameters parameters = new OpenAiChatParameters();
		parameters.setModel("gpt-3.5-turbo");

		final Chain<Map<String, String>, String> chain = 
				new OpenAiChatChain("Hello, this is ${name}. What is your name?", parameters)
						.chain(prev -> Collections.singletonMap("result", prev))
						.chain(new OpenAiChatChain("What was the question for the following answer: ${result}", parameters));

		final String result = chain.run(Collections.singletonMap("name", "Manuel"));
		System.out.println(result);
	}
}
