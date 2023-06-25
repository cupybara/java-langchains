package com.github.hakenadu.javalangchains.chains.data.writer;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.chains.data.reader.ReadDocumentsFromPdfChain;

/**
 * tests for the {@link WriteDocumentsToElasticsearchIndexChain}
 */
class WriteDocumentsToElasticsearchIndexChainIT {

	@Test
	void testRun() throws URISyntaxException {
		Chain<Path, Void> fillElasticsearchIndexChain = new ReadDocumentsFromPdfChain()
				.chain(new WriteDocumentsToElasticsearchIndexChain("my-index"));

		Path pdfDirectoryPath = Paths.get(WriteDocumentsToElasticsearchIndexChainIT.class.getResource("/pdf").toURI());

		fillElasticsearchIndexChain.run(pdfDirectoryPath);
	}
}
