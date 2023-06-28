package com.github.hakenadu.javalangchains.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.chains.base.JoinChain;
import com.github.hakenadu.javalangchains.chains.base.StreamUnwrappingChain;
import com.github.hakenadu.javalangchains.chains.base.StreamWrappingChain;
import com.github.hakenadu.javalangchains.chains.data.reader.ReadDocumentsFromPdfChain;
import com.github.hakenadu.javalangchains.chains.data.retrieval.LuceneRetrievalChain;
import com.github.hakenadu.javalangchains.chains.data.writer.WriteDocumentsToLuceneDirectoryChain;
import com.github.hakenadu.javalangchains.chains.llm.openai.chat.OpenAiChatCompletionsChain;
import com.github.hakenadu.javalangchains.chains.llm.openai.chat.OpenAiChatCompletionsParameters;
import com.github.hakenadu.javalangchains.chains.qa.CombineDocumentsChain;
import com.github.hakenadu.javalangchains.chains.qa.ModifyDocumentsContentChain;
import com.github.hakenadu.javalangchains.chains.qa.split.JtokkitTextSplitter;
import com.github.hakenadu.javalangchains.chains.qa.split.SplitDocumentsChain;
import com.github.hakenadu.javalangchains.util.PromptConstants;
import com.github.hakenadu.javalangchains.util.PromptTemplates;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.EncodingType;

/**
 * tests for a complete document comparing {@link Chain}
 * 
 * we'll read two insurance policies and compare them by querying
 */
class DocumentComparisonTest {

	private static Path tempIndexPath;
	private static Directory directory;
	private static Set<String> pdfSources;

	@BeforeAll
	static void beforeAll() throws IOException, URISyntaxException {
		tempIndexPath = Files.createTempDirectory("lucene");
		pdfSources = new LinkedHashSet<>();

		/*
		 * We are also using a chain to create the lucene index directory
		 */
		final Chain<Path, Directory> createLuceneIndexChain = new ReadDocumentsFromPdfChain()
				// utility chain for storing all different pdf sources (multi compare)
				.chain(readDocuments -> readDocuments.map(doc -> {
					pdfSources.add(doc.get(PromptConstants.SOURCE));
					return doc;
				}))
				// Optional Chain: split pdfs based on a max token count of 1000
				.chain(new SplitDocumentsChain(new JtokkitTextSplitter(
						Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE), 500)))
				// Mandatory Chain: write split pdf documents to a lucene directory
				.chain(new WriteDocumentsToLuceneDirectoryChain(tempIndexPath));

		final Path pdfDirectoryPath = Paths.get(RetrievalQaTest.class.getResource("/pdf/comparison").toURI());

		directory = createLuceneIndexChain.run(pdfDirectoryPath);
	}

	@AfterAll
	static void afterAll() throws IOException {
		directory.close();
		Files.walk(tempIndexPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

	@Test
	void testDocumentComparison() throws IOException {
		final List<Chain<String, Map<String, String>>> retrievalChains = new LinkedList<>();
		for (final String pdfSource : pdfSources) {
			retrievalChains.add(createRetrievalChain(pdfSource));
		}

		final Chain<String, String> documentComparisonChain = new JoinChain<>(true /* parallel */, retrievalChains)
				.chain(new CombineDocumentsChain(PromptTemplates.QA_DOCUMENT))
				.chain(new OpenAiChatCompletionsChain(PromptTemplates.COMPARE_MULTIPLE_DOCUMENTS,
						new OpenAiChatCompletionsParameters().temperature(0).model("gpt-3.5-turbo"),
						System.getenv("OPENAI_API_KEY")));

		final String result = documentComparisonChain.run("to which extent are personal belongings covered?");
		assertNotNull(result, "got null result");
		
		System.out.println(result);
	}

	private Chain<String, Map<String, String>> createRetrievalChain(final String pdfSource) {
		final LuceneRetrievalChain retrievalChain = new LuceneRetrievalChain(directory, 1,
				content -> createQuery(pdfSource, content));

		final ModifyDocumentsContentChain summarizeDocumentsChain = new ModifyDocumentsContentChain(
				new OpenAiChatCompletionsChain(PromptTemplates.QA_SUMMARIZE,
						new OpenAiChatCompletionsParameters().temperature(0).model("gpt-3.5-turbo"),
						System.getenv("OPENAI_API_KEY")));

		final CombineDocumentsChain combineDocumentsChain = new CombineDocumentsChain();

		final ModifyDocumentsContentChain compareChain = new ModifyDocumentsContentChain(new OpenAiChatCompletionsChain(
				PromptTemplates.QA_COMPARE, new OpenAiChatCompletionsParameters().temperature(0).model("gpt-3.5-turbo"),
				System.getenv("OPENAI_API_KEY")));

		// @formatter:off
		return retrievalChain
			.chain(summarizeDocumentsChain)
			.chain(combineDocumentsChain)
			.chain(new StreamWrappingChain<>())
			.chain(compareChain)
			.chain(new StreamUnwrappingChain<>())
			.chain(llmOutput -> {
				final Map<String, String> document = new LinkedHashMap<>(llmOutput);
				document.put(PromptConstants.SOURCE, pdfSource);
				return document;
			});
		// @formatter:on
	}

	private Query createQuery(final String source, final String searchTerm) {
		final StandardAnalyzer analyzer = new StandardAnalyzer();

		final QueryParser contentQueryParser = new QueryParser(PromptConstants.CONTENT, analyzer);
		final QueryParser sourceQueryParser = new QueryParser(PromptConstants.SOURCE, analyzer);

		try {
			final Query contentQuery = contentQueryParser.parse(searchTerm);
			final Query sourceQuery = sourceQueryParser.parse(source);

			// @formatter:off
			return new BooleanQuery.Builder()
					.add(sourceQuery, BooleanClause.Occur.MUST)
					.add(contentQuery, BooleanClause.Occur.SHOULD)
					.build();
			// @formatter:on
		} catch (final ParseException parseException) {
			throw new IllegalStateException("error creating query", parseException);
		}
	}
}
