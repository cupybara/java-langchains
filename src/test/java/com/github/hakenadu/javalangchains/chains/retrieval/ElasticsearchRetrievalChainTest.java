package com.github.hakenadu.javalangchains.chains.retrieval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.chains.data.reader.ReadDocumentsFromPdfChain;
import com.github.hakenadu.javalangchains.chains.data.writer.WriteDocumentsToElasticsearchIndexChain;
import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * Tests for the {@link ElasticsearchRetrievalChain}
 */
class ElasticsearchRetrievalChainTest {

	private static RestClientBuilder restClientBuilder;

	@BeforeAll
	static void beforeAll() throws URISyntaxException {
		restClientBuilder = RestClient.builder(new HttpHost("localhost", 9200));

		final Chain<Path, Void> createElasticsearchIndexChain = new ReadDocumentsFromPdfChain()
				.chain(new WriteDocumentsToElasticsearchIndexChain("my-index", restClientBuilder));

		final Path pdfDirectoryPath = Paths.get(ElasticsearchRetrievalChainTest.class.getResource("/pdf").toURI());

		// creates and fills the elasticsearch index "my-index"
		createElasticsearchIndexChain.run(pdfDirectoryPath);
	}

	@Test
	void testRun() throws IOException {
		try (final RestClient restClient = restClientBuilder.build();
				final ElasticsearchRetrievalChain retrievalChain = new ElasticsearchRetrievalChain("my-index",
						restClient, 1)) {

			final List<Map<String, String>> retrievedDocuments = retrievalChain.run("who is john doe?")
					.collect(Collectors.toList());
			assertEquals(1, retrievedDocuments.size(), "incorrect number of retrieved documents");

			final Map<String, String> document = retrievedDocuments.get(0);

			assertTrue(document.containsKey(PromptConstants.QUESTION), "document does not contain question key");
			assertTrue(document.containsKey(PromptConstants.SOURCE), "document does not contain source key");
			assertTrue(document.containsKey(PromptConstants.CONTENT), "document does not contain content key");

			assertEquals("who is john doe?", document.get(PromptConstants.QUESTION), "wrong question in document");
		}
	}
}
