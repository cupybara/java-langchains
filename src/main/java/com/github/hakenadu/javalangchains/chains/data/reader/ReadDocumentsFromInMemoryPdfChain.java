package com.github.hakenadu.javalangchains.chains.data.reader;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.pdfbox.Loader;

import com.github.hakenadu.javalangchains.chains.data.reader.ReadDocumentsFromInMemoryPdfChain.InMemoryPdf;

/**
 * Utilizes Apache PDFBox to read documents from a byte array
 */
public class ReadDocumentsFromInMemoryPdfChain extends ReadDocumentsFromPdfChainBase<InMemoryPdf> {

	/**
	 * wrapper for an in memory pdf (byte array + title)
	 */
	public static class InMemoryPdf {

		/**
		 * pdf data as byte array
		 */
		private final byte[] data;

		/**
		 * pdf document name
		 */
		private final String name;

		/**
		 * @param data {@link #data}
		 * @param name {@link #name}
		 */
		public InMemoryPdf(final byte[] data, final String name) {
			this.data = data;
			this.name = name;
		}
	}

	/**
	 * creates a {@link ReadDocumentsFromInMemoryPdfChain}
	 * 
	 * @param readMode {@link #readMode}
	 * @param parallel {@link #parallel}
	 */
	public ReadDocumentsFromInMemoryPdfChain(final PdfReadMode readMode, final boolean parallel) {
		super(readMode, parallel);
	}

	/**
	 * creates a {@link ReadDocumentsFromInMemoryPdfChain}
	 * 
	 * @param readMode {@link #readMode}
	 */
	public ReadDocumentsFromInMemoryPdfChain(final PdfReadMode readMode) {
		this(readMode, false);
	}

	/**
	 * creates a {@link ReadDocumentsFromInMemoryPdfChain} which reads the whole pdf
	 * as a document
	 */
	public ReadDocumentsFromInMemoryPdfChain() {
		this(PdfReadMode.WHOLE);
	}

	@Override
	protected Stream<PdDocumentWrapper> loadPdDocuments(final InMemoryPdf input) throws IOException {
		return Stream.of(new PdDocumentWrapper(Loader.loadPDF(input.data), input.name));
	}
}
