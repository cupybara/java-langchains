package io.github.cupybara.javalangchains.chains.data.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.pdfbox.Loader;

/**
 * Utilizes Apache PDFBox to read documents from a PDF or a folder of PDFs
 */
public class ReadDocumentsFromPdfChain extends ReadDocumentsFromPdfChainBase<Path> {

	/**
	 * creates a {@link ReadDocumentsFromPdfChain}
	 * 
	 * @param readMode {@link #readMode}
	 * @param parallel {@link #parallel}
	 */
	public ReadDocumentsFromPdfChain(final PdfReadMode readMode, final boolean parallel) {
		super(readMode, parallel);
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
	protected Stream<PdDocumentWrapper> loadPdDocuments(final Path input) throws IOException {
		return Files.walk(input).filter(Files::isRegularFile)
				.filter(path -> path.toString().toLowerCase().endsWith(".pdf")).map(path -> {
					try {
						return new PdDocumentWrapper(Loader.loadPDF(path.toFile()), path.getFileName().toString());
					} catch (final IOException ioException) {
						throw new IllegalStateException("could not read document from " + path);
					}
				});
	}
}
