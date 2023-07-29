package io.github.cupybara.javalangchains.chains.data.writer;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.cupybara.javalangchains.chains.Chain;
import io.github.cupybara.javalangchains.chains.data.retrieval.ElasticsearchRetrievalChain;
import io.github.cupybara.javalangchains.util.PromptConstants;

/**
 * Inserts documents into an elasticsearch index
 */
public class WriteDocumentsToElasticsearchIndexChain implements Chain<Stream<Map<String, String>>, Void> {

	/**
	 * Elasticsearch index name
	 */
	private final String index;

	/**
	 * Elasticsearch Low Level {@link RestClientBuilder}
	 */
	private final RestClientBuilder restClientBuilder;

	/**
	 * Optional {@link Function} which provides an ID value for a document. If set,
	 * documents are indexed using PUT /_doc/${id} instead of POST /_doc
	 */
	private final Function<Map<String, String>, String> idProvider;

	/**
	 * creates an elasticsearch index by consuming its name and an already
	 * instantiated {@link RestClient}
	 */
	private final BiConsumer<String, RestClient> indexCreator;

	/**
	 * creates the effective document json from an input document
	 */
	private final Function<Map<String, String>, String> documentJsonCreator;

	/**
	 * @param index               {@link #index}
	 * @param restClientBuilder   {@link #restClientBuilder}
	 * @param idProvider          {@link #idProvider}
	 * @param indexCreator        {@link #indexCreator}
	 * @param documentJsonCreator {@link #documentJsonCreator}
	 */
	public WriteDocumentsToElasticsearchIndexChain(final String index, final RestClientBuilder restClientBuilder,
			final Function<Map<String, String>, String> idProvider, final BiConsumer<String, RestClient> indexCreator,
			final Function<Map<String, String>, String> documentJsonCreator) {
		this.index = index;
		this.restClientBuilder = restClientBuilder;
		this.idProvider = idProvider;
		this.indexCreator = indexCreator;
		this.documentJsonCreator = documentJsonCreator;
	}

	/**
	 * @param index             {@link #index}
	 * @param restClientBuilder {@link #restClientBuilder}
	 * @param objectMapper      {@link ObjectMapper} used to create default json
	 *                          operations
	 * @param idProvider        {@link #idProvider}
	 * @param indexCreator      {@link #indexCreator}
	 */
	public WriteDocumentsToElasticsearchIndexChain(final String index, final RestClientBuilder restClientBuilder,
			final ObjectMapper objectMapper, final Function<Map<String, String>, String> idProvider,
			final BiConsumer<String, RestClient> indexCreator) {
		this(index, restClientBuilder, idProvider, defaultIndexCreator(objectMapper),
				defaultDocumentJsonCreator(objectMapper));
	}

	/**
	 * @param index             {@link #index}
	 * @param restClientBuilder {@link #restClientBuilder}
	 * @param objectMapper      {@link ObjectMapper} used to create default json
	 *                          operations
	 * @param idProvider        {@link #idProvider}
	 */
	public WriteDocumentsToElasticsearchIndexChain(final String index, final RestClientBuilder restClientBuilder,
			final ObjectMapper objectMapper, final Function<Map<String, String>, String> idProvider) {
		this(index, restClientBuilder, objectMapper, idProvider, defaultIndexCreator(objectMapper));
	}

	/**
	 * @param index             {@link #index}
	 * @param restClientBuilder {@link #restClientBuilder}
	 * @param objectMapper      {@link ObjectMapper} used to create default json
	 *                          operations
	 */
	public WriteDocumentsToElasticsearchIndexChain(final String index, final RestClientBuilder restClientBuilder,
			final ObjectMapper objectMapper) {
		this(index, restClientBuilder, objectMapper, null);
	}

	/**
	 * creates a {@link WriteDocumentsToElasticsearchIndexChain} with the default
	 * {@link ObjectMapper}
	 * 
	 * @param index             {@link #index}
	 * @param restClientBuilder {@link #restClientBuilder}
	 */
	public WriteDocumentsToElasticsearchIndexChain(final String index, final RestClientBuilder restClientBuilder) {
		this(index, restClientBuilder, new ObjectMapper());
	}

	/**
	 * creates a {@link WriteDocumentsToElasticsearchIndexChain} with the default
	 * {@link HttpHost} (http://localhost:9200) and a default {@link ObjectMapper}
	 * 
	 * @param index {@link #index}
	 */
	public WriteDocumentsToElasticsearchIndexChain(final String index) {
		this(index, RestClient.builder(new HttpHost("localhost", 9200)));
	}

	@Override
	public Void run(final Stream<Map<String, String>> input) {
		try (final RestClient restClient = restClientBuilder.build()) {

			if (this.indexCreator != null) {
				createIndexIfNotExists(restClient);
			}

			input.forEach(document -> {
				final String documentJson = documentJsonCreator.apply(document);

				final Request indexRequest = createIndexRequest(document);
				indexRequest.setJsonEntity(documentJson);
				try {
					restClient.performRequest(indexRequest);
				} catch (final IOException innerIoException) {
					throw new IllegalStateException("error writing document " + documentJson, innerIoException);
				}
			});

		} catch (final IOException ioException) {
			throw new IllegalStateException("error writing documents to elasticsearch index", ioException);
		}
		return null;
	}

	private Request createIndexRequest(final Map<String, String> document) {
		if (idProvider == null) {
			return new Request("POST", String.format("/%s/_doc", index));
		} else {
			final String id = idProvider.apply(document);
			LogManager.getLogger(getClass()).debug("creating document with id {}", id);
			return new Request("PUT", String.format("/%s/_doc/%s", index, id));
		}
	}

	/**
	 * Checks whether an index with the name {@link #index} exists. If none exists,
	 * it is created with default settings used for
	 * {@link ElasticsearchRetrievalChain}.
	 * 
	 * @param restClient the {@link RestClient} used to perform elasticsearch
	 *                   requests
	 * @throws IOException on error
	 */
	private synchronized void createIndexIfNotExists(final RestClient restClient) throws IOException {
		final Request indexExistsRequest = new Request("HEAD", '/' + index);
		final Response indexExistsResponse = restClient.performRequest(indexExistsRequest);
		if (indexExistsResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			LogManager.getLogger(getClass()).info("index {} exists", index);
			return;
		}

		LogManager.getLogger(getClass()).info("creating index {} with default settings", index);

		this.indexCreator.accept(index, restClient);
	}

	/**
	 * creates the default {@link Function} for creating json documents from input
	 * documents for this chain
	 * 
	 * @param objectMapper {@link ObjectMapper} for json operations
	 * @return default {@link #documentJsonCreator}
	 */
	public static Function<Map<String, String>, String> defaultDocumentJsonCreator(final ObjectMapper objectMapper) {
		return document -> {
			try {
				return objectMapper.writeValueAsString(document);
			} catch (final JsonProcessingException jsonProcessingException) {
				throw new IllegalStateException("error creating json for document " + document,
						jsonProcessingException);
			}
		};
	}

	/**
	 * Realizes the default way of creating an elasticsearch index using the method
	 * from
	 * https://github.com/hwchase17/langchain/blob/master/langchain/retrievers/elastic_search_bm25.py
	 * 
	 * @param objectMapper {@link ObjectMapper} for json operations
	 * @return default {@link #indexCreator}
	 */
	public static BiConsumer<String, RestClient> defaultIndexCreator(final ObjectMapper objectMapper) {
		return (indexName, restClient) -> {
			final ObjectNode indexRequestBody = objectMapper.createObjectNode();

			// "settings": {...}
			final ObjectNode settings = indexRequestBody.putObject("settings");

			// "analysis": {"analyzer": {"default": {"type": "standard"}}}
			settings.putObject("analysis").putObject("analyzer").putObject("default").put("type", "standard");

			// "similarity": {"custom_bm25": {"type": "BM25", "k1": 2.0, "b": 0.75}
			settings.putObject("similarity").putObject("custom_bm25").put("type", "BM25").put("k1", 2.0).put("b", 0.75);

			// "mappings": {"properties": {"content": {"type": "text", "similarity":
			// "custom_bm25"}}}
			indexRequestBody.putObject("mappings").putObject("properties").putObject(PromptConstants.CONTENT)
					.put("type", "text").put("similarity", "custom_bm25");

			final String indexRequestBodyJson = indexRequestBody.toString();
			final Request indexRequest = new Request("PUT", '/' + indexName);
			indexRequest.setJsonEntity(indexRequestBodyJson);
			try {
				restClient.performRequest(indexRequest);
			} catch (final IOException ioException) {
				throw new IllegalStateException("error creating index " + indexRequestBodyJson, ioException);
			}
		};
	}
}
