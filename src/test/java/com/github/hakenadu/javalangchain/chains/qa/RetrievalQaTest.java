package com.github.hakenadu.javalangchain.chains.qa;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchain.chains.Chain;
import com.github.hakenadu.javalangchain.chains.llm.openai.OpenAiChatChain;
import com.github.hakenadu.javalangchain.chains.llm.openai.OpenAiChatParameters;
import com.github.hakenadu.javalangchain.chains.retrieval.CombineDocumentsChain;
import com.github.hakenadu.javalangchain.chains.retrieval.lucene.LuceneRetrievalChain;
import com.github.hakenadu.javalangchain.chains.retrieval.lucene.LuceneRetrievalChainTest;
import com.github.hakenadu.javalangchain.util.PromptTemplates;

/**
 * tests for a complete qa {@link Chain}
 */
class RetrievalQaTest {

	private static Path tempDirPath;
	private static Directory directory;

	@BeforeAll
	public static void beforeAll() throws IOException {
		tempDirPath = Files.createTempDirectory("lucene");
		directory = new MMapDirectory(tempDirPath);
		LuceneRetrievalChainTest.fillDirectory(directory);
	}

	@AfterAll
	public static void afterAll() throws IOException {
		directory.close();
		Files.walk(tempDirPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

	@Test
	public void testQa() throws IOException {
		/*
		 * Chain 1: The retrievalChain is used to retrieve relevant documents from an
		 * index by using bm25 similarity
		 */
		try (final LuceneRetrievalChain retrievalChain = new LuceneRetrievalChain(directory, 2)) {

			/*
			 * Chain 2: The combineDocumentsChain is used to combine the retrieved documents
			 * in a single prompt
			 */
			final CombineDocumentsChain combineDocumentsChain = new CombineDocumentsChain();

			/*
			 * Chain 3: The openAiChatChain is used to process the combined prompt using an
			 * OpenAI LLM (gpt-3.5-turbo in this case)
			 */
			final OpenAiChatChain openAiChatChain = new OpenAiChatChain(PromptTemplates.QA_COMBINE,
					new OpenAiChatParameters().model("gpt-3.5-turbo"), System.getenv("OPENAI_API_KEY"));

			// we combine all chain links into a self contained QA chain
			final Chain<String, String> qaChain = retrievalChain.chain(combineDocumentsChain).chain(openAiChatChain);

			// the QA chain can now be called with a question and delivers an answer
			final String answer = qaChain.run("what is john's art gallery called?");
			assertNotNull(answer, "no answer provided");

			LogManager.getLogger().info(answer);
		}
	}
}
