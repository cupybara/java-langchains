package com.github.hakenadu.javalangchains.chains.data.reader;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.util.PromptConstants;

/**
 * provides base functionality for all pdf reading chains
 *
 * @param <I> input type to read pdfs from
 */
public abstract class ReadDocumentsFromPdfChainBase<I> implements Chain<I, Stream<Map<String, String>>> {

	/**
	 * this enum is used to configure how each pdf content is read into a string
	 */
	public enum PdfReadMode {
		/**
		 * Reads the whole document into a string
		 */
		WHOLE,

		/**
		 * Reads each document page by page: provides a list of documents for each
		 * document and adds "p. ${pageIndex}" to each "source" field
		 */
		PAGES;
	}

	/**
	 * (PDDocument, PDF-Name) pair
	 */
	protected class PdDocumentWrapper {
		private final PDDocument pdDocument;
		private final String pdDocumentName;

		/**
		 * creates an instance of PdDocumentWrapper
		 * 
		 * @param pdDocument     {@link #pdDocument}
		 * @param pdDocumentName {@link #pdDocumentName}
		 */
		protected PdDocumentWrapper(final PDDocument pdDocument, final String pdDocumentName) {
			this.pdDocument = pdDocument;
			this.pdDocumentName = pdDocumentName;
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
	 * creates a {@link ReadDocumentsFromPdfChainBase}
	 * 
	 * @param readMode {@link #readMode}
	 * @param parallel {@link #parallel}
	 */
	protected ReadDocumentsFromPdfChainBase(final PdfReadMode readMode, final boolean parallel) {
		this.readMode = readMode;
		this.parallel = parallel;
	}

	/**
	 * load a pdf from an input instance
	 * 
	 * @param input input instance
	 * @return {@link PDDocument}
	 * 
	 * @throws IOException on error loading pdf
	 */
	protected abstract Stream<PdDocumentWrapper> loadPdDocuments(I input) throws IOException;

	@Override
	public Stream<Map<String, String>> run(final I input) {
		final Stream<Map<String, String>> documents;

		try {
			documents = loadPdDocuments(input).flatMap(this::createDocumentFromPdDocumentWrapper);
		} catch (final IOException ioException) {
			throw new IllegalStateException("error loading pdf for input " + input, ioException);
		}

		if (parallel) {
			return documents.parallel();
		}

		return documents;
	}

	private Stream<Map<String, String>> createDocumentFromPdDocumentWrapper(final PdDocumentWrapper pdDocumentWrapper) {
		try {
			switch (readMode) {
			case WHOLE:
				return Stream.of(createDocumentFromWholePdf(pdDocumentWrapper));
			case PAGES:
				return createDocumentsFromPdfPages(pdDocumentWrapper).stream();
			default:
				throw new IllegalStateException("unsupported readMode " + readMode);
			}
		} catch (final IOException innerIoException) {
			throw new IllegalStateException("could not create documents", innerIoException);
		} finally {
			try {
				pdDocumentWrapper.pdDocument.close();
			} catch (final IOException ioException) {
				throw new IllegalStateException("could not close PDDocument", ioException);
			}
		}
	}

	private Map<String, String> createDocumentFromWholePdf(final PdDocumentWrapper pdDocumentWrapper)
			throws IOException {

		final PDFTextStripper textStripper = new PDFTextStripper();

		final String content = textStripper.getText(pdDocumentWrapper.pdDocument);

		final Map<String, String> document = new LinkedHashMap<>();
		document.put(PromptConstants.CONTENT, content);
		document.put(PromptConstants.SOURCE, pdDocumentWrapper.pdDocumentName);

		LogManager.getLogger().info("successfully read document {}", pdDocumentWrapper.pdDocumentName);

		return document;
	}

	private List<Map<String, String>> createDocumentsFromPdfPages(final PdDocumentWrapper pdDocumentWrapper)
			throws IOException {
		final PDFTextStripper textStripper = new PDFTextStripper();

		final List<Map<String, String>> documents = new LinkedList<>();
		for (int pageIndex = 0; pageIndex < pdDocumentWrapper.pdDocument.getNumberOfPages(); pageIndex++) {
			textStripper.setStartPage(pageIndex + 1);
			textStripper.setEndPage(pageIndex + 1);

			final String pageContent;
			try {
				pageContent = textStripper.getText(pdDocumentWrapper.pdDocument);
			} catch (final IOException innerIoException) {
				throw new IllegalStateException("error reading page with index " + pageIndex, innerIoException);
			}

			final int pageNumber = pageIndex + 1;

			final Map<String, String> pageDocument = new LinkedHashMap<>();
			pageDocument.put(PromptConstants.CONTENT, pageContent);
			pageDocument.put(PromptConstants.SOURCE,
					String.format("%s p.%d", pdDocumentWrapper.pdDocumentName, pageNumber));

			LogManager.getLogger().info("successfully read page {} of document {}", pageNumber,
					pdDocumentWrapper.pdDocumentName);

			documents.add(pageDocument);
		}

		return documents;
	}
}
