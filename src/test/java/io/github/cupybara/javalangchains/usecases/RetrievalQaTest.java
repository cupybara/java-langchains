package io.github.cupybara.javalangchains.usecases;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.EncodingType;

import io.github.cupybara.javalangchains.chains.Chain;
import io.github.cupybara.javalangchains.chains.base.ApplyToStreamInputChain;
import io.github.cupybara.javalangchains.chains.base.logging.LoggingChain;
import io.github.cupybara.javalangchains.chains.data.reader.ReadDocumentsFromPdfChain;
import io.github.cupybara.javalangchains.chains.data.retrieval.LuceneRetrievalChain;
import io.github.cupybara.javalangchains.chains.data.writer.WriteDocumentsToLuceneDirectoryChain;
import io.github.cupybara.javalangchains.chains.llm.openai.chat.OpenAiChatCompletionsChain;
import io.github.cupybara.javalangchains.chains.llm.openai.chat.OpenAiChatCompletionsParameters;
import io.github.cupybara.javalangchains.chains.qa.AnswerWithSources;
import io.github.cupybara.javalangchains.chains.qa.CombineDocumentsChain;
import io.github.cupybara.javalangchains.chains.qa.MapAnswerWithSourcesChain;
import io.github.cupybara.javalangchains.chains.qa.ModifyDocumentsContentChain;
import io.github.cupybara.javalangchains.chains.qa.split.JtokkitTextSplitter;
import io.github.cupybara.javalangchains.chains.qa.split.SplitDocumentsChain;
import io.github.cupybara.javalangchains.util.PromptTemplates;

/**
 * tests for a complete qa {@link Chain}
 * 
 * we'll read documents from our demo john doe pdfs at src/test/resources/pdf
 * and then ask questions about the protagonist.
 */
class RetrievalQaTest {

	private static Path tempIndexPath;
	private static Directory directory;

	@BeforeAll
	static void beforeAll() throws IOException, URISyntaxException {
		tempIndexPath = Files.createTempDirectory("lucene");

		/*
		 * We are also using a chain to create the lucene index directory
		 */
		final Chain<Path, Directory> createLuceneIndexChain = new ReadDocumentsFromPdfChain()
				// Optional Chain: split pdfs based on a max token count of 1000
				.chain(new SplitDocumentsChain(new JtokkitTextSplitter(
						Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE), 1000)))
				// Mandatory Chain: write split pdf documents to a lucene directory
				.chain(new WriteDocumentsToLuceneDirectoryChain(tempIndexPath));

		final Path pdfDirectoryPath = Paths.get(RetrievalQaTest.class.getResource("/pdf/qa").toURI());

		directory = createLuceneIndexChain.run(pdfDirectoryPath);
	}

	@AfterAll
	static void afterAll() throws IOException {
		directory.close();
		Files.walk(tempIndexPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

	@Test
	void testQa() throws IOException {
		final OpenAiChatCompletionsParameters openAiChatParameters = new OpenAiChatCompletionsParameters()
				.temperature(0D).model("gpt-3.5-turbo");

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
			final ModifyDocumentsContentChain summarizeDocumentsChain = new ModifyDocumentsContentChain(
					new OpenAiChatCompletionsChain(PromptTemplates.QA_SUMMARIZE, openAiChatParameters,
							System.getenv("OPENAI_API_KEY")));

			/*
			 * Chain 3: The combineDocumentsChain is used to combine the retrieved documents
			 * in a single prompt
			 */
			final CombineDocumentsChain combineDocumentsChain = new CombineDocumentsChain();

			/*
			 * Chain 4: The openAiChatChain is used to process the combined prompt using an
			 * OpenAI LLM (gpt-3.5-turbo in this case)
			 */
			final OpenAiChatCompletionsChain openAiChatChain = new OpenAiChatCompletionsChain(
					PromptTemplates.QA_COMBINE, openAiChatParameters, System.getenv("OPENAI_API_KEY"));

			/*
			 * Chain 5: The mapAnswerWithSourcesChain is used to map the llm string output
			 * to a complex object using a regular expression which splits the sources and
			 * the answer.
			 */
			final MapAnswerWithSourcesChain mapAnswerWithSourcesChain = new MapAnswerWithSourcesChain();

			// @formatter:off
			// we combine all chain links into a self contained QA chain
			final Chain<String, AnswerWithSources> qaChain = retrievalChain
					.chain(summarizeDocumentsChain)
						.chain(new ApplyToStreamInputChain<>(new LoggingChain<>(LoggingChain.defaultLogPrefix("SUMMARIZED_DOCUMENT"))))
					.chain(combineDocumentsChain)
						.chain(new LoggingChain<>(LoggingChain.defaultLogPrefix("COMBINED_DOCUMENT")))
					.chain(openAiChatChain)
						.chain(new LoggingChain<>(LoggingChain.defaultLogPrefix("LLM_RESULT")))
					.chain(mapAnswerWithSourcesChain);
			// @formatter:on

			// the QA chain can now be called with a question and delivers an answer
			final AnswerWithSources answerToValidQuestion = qaChain.run("who is john doe?");
			assertNotNull(answerToValidQuestion, "no answer provided");
			assertFalse(answerToValidQuestion.getSources().isEmpty(), "no sources");
			LogManager.getLogger().info("answer to valid question: {}", answerToValidQuestion);
		}
	}
}
