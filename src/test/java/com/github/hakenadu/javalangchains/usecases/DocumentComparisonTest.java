package com.github.hakenadu.javalangchains.usecases;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.chains.data.reader.ReadDocumentsFromPdfChain;
import com.github.hakenadu.javalangchains.chains.data.writer.WriteDocumentsToLuceneDirectoryChain;
import com.github.hakenadu.javalangchains.chains.qa.split.JtokkitTextSplitter;
import com.github.hakenadu.javalangchains.chains.qa.split.SplitDocumentsChain;
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

		final Path pdfDirectoryPath = Paths.get(RetrievalQaTest.class.getResource("/pdf/comparison").toURI());

		directory = createLuceneIndexChain.run(pdfDirectoryPath);
	}

	@AfterAll
	static void afterAll() throws IOException {
		directory.close();
		Files.walk(tempIndexPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

	@Test
	void testDocumentComparison() {
		// TODO
	}
}
