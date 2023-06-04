package com.github.hakenadu.javalangchains.chains.data.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * Utilizes Apache PDFBox to read documents from a folder of PDFs
 */
public class ReadDocumentsFromPdfChain implements Chain<Path, Stream<Map<String, String>>> {

	@Override
	public Stream<Map<String, String>> run(final Path pathToPdfDirectory) {
		if (!Files.isDirectory(pathToPdfDirectory)) {
			throw new IllegalArgumentException("not a directory: " + pathToPdfDirectory.toAbsolutePath());
		}

		try {
			return Files.walk(pathToPdfDirectory).filter(Files::isRegularFile)
					.filter(path -> path.toString().toLowerCase().endsWith(".pdf")).map(this::createDocumentFromPdf);
		} catch (final IOException ioException) {
			throw new IllegalStateException("error reading documents", ioException);
		}
	}

	private Map<String, String> createDocumentFromPdf(final Path pdfPath) {
		final String content;

		try (final PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
			final PDFTextStripper textStripper = new PDFTextStripper();
			content = textStripper.getText(document);
		} catch (final IOException ioException) {
			throw new IllegalStateException("error reading " + pdfPath.toAbsolutePath(), ioException);
		}

		final Map<String, String> document = new LinkedHashMap<>();
		document.put(PromptConstants.CONTENT, content);
		document.put(PromptConstants.SOURCE, pdfPath.getFileName().toString());

		LOGGER.info("successfully read document {}", pdfPath.getFileName());

		return document;
	}
}
