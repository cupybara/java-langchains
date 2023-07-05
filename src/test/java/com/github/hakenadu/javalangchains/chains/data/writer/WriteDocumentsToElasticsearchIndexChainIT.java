package com.github.hakenadu.javalangchains.chains.data.writer;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.chains.data.reader.ReadDocumentsFromPdfChain;
import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * tests for the {@link WriteDocumentsToElasticsearchIndexChain}
 */
class WriteDocumentsToElasticsearchIndexChainIT {

	@Test
	void testRun() throws URISyntaxException {
		Chain<Path, Void> fillElasticsearchIndexChain = new ReadDocumentsFromPdfChain()
				.chain(new WriteDocumentsToElasticsearchIndexChain("my-index"));

		Path pdfDirectoryPath = Paths
				.get(WriteDocumentsToElasticsearchIndexChainIT.class.getResource("/pdf/qa").toURI());

		fillElasticsearchIndexChain.run(pdfDirectoryPath);
	}

	@Test
	void testRunSpecificId() throws URISyntaxException {
		WriteDocumentsToElasticsearchIndexChain writeChain = new WriteDocumentsToElasticsearchIndexChain("my-index",
				RestClient.builder(new HttpHost("localhost", 9200)), new ObjectMapper(),
				doc -> Base64.getEncoder().encodeToString(doc.get(PromptConstants.SOURCE).getBytes()));

		Chain<Path, Void> fillElasticsearchIndexChain = new ReadDocumentsFromPdfChain().chain(writeChain);

		Path pdfDirectoryPath = Paths
				.get(WriteDocumentsToElasticsearchIndexChainIT.class.getResource("/pdf/qa").toURI());

		fillElasticsearchIndexChain.run(pdfDirectoryPath);
	}
}
