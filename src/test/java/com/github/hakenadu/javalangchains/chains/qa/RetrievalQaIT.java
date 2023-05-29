package com.github.hakenadu.javalangchains.chains.qa;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.chains.llm.openai.OpenAiChatChain;
import com.github.hakenadu.javalangchains.chains.llm.openai.OpenAiChatParameters;
import com.github.hakenadu.javalangchains.chains.retrieval.AnswerWithSources;
import com.github.hakenadu.javalangchains.chains.retrieval.CombineDocumentsChain;
import com.github.hakenadu.javalangchains.chains.retrieval.MapAnswerWithSourcesChain;
import com.github.hakenadu.javalangchains.chains.retrieval.SummarizeDocumentsChain;
import com.github.hakenadu.javalangchains.chains.retrieval.lucene.LuceneRetrievalChain;
import com.github.hakenadu.javalangchains.chains.retrieval.lucene.LuceneRetrievalChainTest;
import com.github.hakenadu.javalangchains.util.PromptTemplates;

/**
 * tests for a complete qa {@link Chain}
 */
class RetrievalQaIT {

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
		final OpenAiChatParameters openAiChatParameters = new OpenAiChatParameters().temperature(0)
				.model("gpt-3.5-turbo");

		/*
		 * Chain 1: The retrievalChain is used to retrieve relevant documents from an
		 * index by using bm25 similarity
		 */
		try (final LuceneRetrievalChain retrievalChain = new LuceneRetrievalChain(directory, 2)) {

			/*
			 * Chain 2: The summarizeDocumentsChain is used to summarize documents to only
			 * contain the most relevant information. This is achieved using an OpenAI LLM
			 * (gpt-3.5-turbo in this case)
			 */
			final SummarizeDocumentsChain summarizeDocumentsChain = new SummarizeDocumentsChain(new OpenAiChatChain(
					PromptTemplates.QA_SUMMARIZE, openAiChatParameters, System.getenv("OPENAI_API_KEY")));

			/*
			 * Chain 3: The combineDocumentsChain is used to combine the retrieved documents
			 * in a single prompt
			 */
			final CombineDocumentsChain combineDocumentsChain = new CombineDocumentsChain();

			/*
			 * Chain 4: The openAiChatChain is used to process the combined prompt using an
			 * OpenAI LLM (gpt-3.5-turbo in this case)
			 */
			final OpenAiChatChain openAiChatChain = new OpenAiChatChain(PromptTemplates.QA_COMBINE,
					openAiChatParameters, System.getenv("OPENAI_API_KEY"));

			/*
			 * Chain 5: The mapAnswerWithSourcesChain is used to map the llm string output
			 * to a complex object using a regular expression which splits the sources and
			 * the answer.
			 */
			final MapAnswerWithSourcesChain mapAnswerWithSourcesChain = new MapAnswerWithSourcesChain();

			// we combine all chain links into a self contained QA chain
			final Chain<String, AnswerWithSources> qaChain = retrievalChain.chain(summarizeDocumentsChain)
					.chain(combineDocumentsChain).chain(openAiChatChain).chain(mapAnswerWithSourcesChain);

			// the QA chain can now be called with a question and delivers an answer
			final AnswerWithSources answerToValidQuestion = qaChain.run("who is john doe?");
			assertNotNull(answerToValidQuestion, "no answer provided");
			assertFalse(answerToValidQuestion.getSources().isEmpty(), "no sources");
			LogManager.getLogger().info("answer to valid question: {}", answerToValidQuestion);
		}
	}
}
