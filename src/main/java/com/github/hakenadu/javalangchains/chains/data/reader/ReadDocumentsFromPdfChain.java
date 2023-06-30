package com.github.hakenadu.javalangchains.chains.data.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * Utilizes Apache PDFBox to read documents from a folder of PDFs
 */
public class ReadDocumentsFromPdfChain implements Chain<Path, Stream<Map<String, String>>> {

	/**
	 * this enum is used to configure how each pdf content is read into a string
	 */
	public static enum PdfReadMode {
		/**
		 * Reads the whole document into a string
		 */
		WHOLE(ReadDocumentsFromPdfChain::createDocumentsFromWholePdf),

		/**
		 * Reads each document page by page => provides a list of documents for each
		 * document and adds "p. ${pageIndex}" to each "source" field
		 */
		PAGES(ReadDocumentsFromPdfChain::createDocumentsFromPdfPages);

		private Function<Path, Stream<Map<String, String>>> documentsCreator;

		private PdfReadMode(final Function<Path, Stream<Map<String, String>>> documentsCreator) {
			this.documentsCreator = documentsCreator;
		}
	}

	/**
	 * @see PdfReadMode
	 */
	private final PdfReadMode readMode;

	/**
	 * if <code>true</code> the reading is done in parallel
	 */
	private final boolean parallel;

	/**
	 * creates a {@link ReadDocumentsFromPdfChain}
	 * 
	 * @param readMode {@link #readMode}*
	 * @param parallel {@link #parallel}
	 */
	public ReadDocumentsFromPdfChain(final PdfReadMode readMode, final boolean parallel) {
		this.readMode = readMode;
		this.parallel = parallel;
	}

	/**
	 * creates a {@link ReadDocumentsFromPdfChain}
	 * 
	 * @param readMode {@link #readMode}
	 */
	public ReadDocumentsFromPdfChain(final PdfReadMode readMode) {
		this(readMode, false);
	}

	/**
	 * creates a {@link ReadDocumentsFromPdfChain} which reads the whole pdf as a
	 * document
	 */
	public ReadDocumentsFromPdfChain() {
		this(PdfReadMode.WHOLE);
	}

	@Override
	public Stream<Map<String, String>> run(final Path pathToPdfDirectory) {
		if (!Files.isDirectory(pathToPdfDirectory)) {
			throw new IllegalArgumentException("not a directory: " + pathToPdfDirectory.toAbsolutePath());
		}

		try {
			final Stream<Map<String, String>> documents = Files.walk(pathToPdfDirectory).filter(Files::isRegularFile)
					.filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
					.flatMap(this.readMode.documentsCreator);

			if (parallel) {
				return documents.parallel();
			}

			return documents;
		} catch (final IOException ioException) {
			throw new IllegalStateException("error reading documents", ioException);
		}
	}

	private static Stream<Map<String, String>> createDocumentsFromWholePdf(final Path pdfPath) {
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

		LogManager.getLogger().info("successfully read document {}", pdfPath.getFileName());

		return Stream.of(document);
	}

	private static Stream<Map<String, String>> createDocumentsFromPdfPages(final Path pdfPath) {
		try (final PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
			final PDFTextStripper textStripper = new PDFTextStripper();

			return IntStream.range(0, document.getNumberOfPages()).mapToObj(pageIndex -> {
				textStripper.setStartPage(pageIndex + 1);
				textStripper.setEndPage(pageIndex + 1);

				final String pageContent;
				try {
					pageContent = textStripper.getText(document);
				} catch (final IOException innerIoException) {
					throw new IllegalStateException("error reading page with index " + pageIndex, innerIoException);
				}

				final int pageNumber = pageIndex + 1;

				final Map<String, String> pageDocument = new LinkedHashMap<>();
				pageDocument.put(PromptConstants.CONTENT, pageContent);
				pageDocument.put(PromptConstants.SOURCE,
						String.format("%s p.%d", pdfPath.getFileName().toString(), pageNumber));

				LogManager.getLogger().info("successfully read page {} of document {}", pageNumber,
						pdfPath.getFileName());

				return pageDocument;
			});

		} catch (final IOException ioException) {
			throw new IllegalStateException("error reading pages from " + pdfPath.toAbsolutePath(), ioException);
		}
	}
}
