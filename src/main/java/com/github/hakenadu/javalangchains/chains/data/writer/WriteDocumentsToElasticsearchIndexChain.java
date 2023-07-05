package com.github.hakenadu.javalangchains.chains.data.writer;

import java.io.IOException;
import java.util.Map;
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
import com.github.hakenadu.javalangchains.chains.Chain;
import com.github.hakenadu.javalangchains.chains.data.retrieval.ElasticsearchRetrievalChain;
import com.github.hakenadu.javalangchains.util.PromptConstants;

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
	 * {@link ObjectMapper} used for document serialization
	 */
	private final ObjectMapper objectMapper;

	/**
	 * Optional {@link Function} which provides an ID value for a document. If set,
	 * documents are indexed using PUT /_doc/${id} instead of POST /_doc
	 */
	private final Function<Map<String, String>, String> idProvider;

	/**
	 * @param index             {@link #index}
	 * @param restClientBuilder {@link #restClientBuilder}
	 * @param objectMapper      {@link #objectMapper}
	 * @param idProvider        {@link #idProvider}
	 */
	public WriteDocumentsToElasticsearchIndexChain(final String index, final RestClientBuilder restClientBuilder,
			final ObjectMapper objectMapper, final Function<Map<String, String>, String> idProvider) {
		this.index = index;
		this.restClientBuilder = restClientBuilder;
		this.objectMapper = objectMapper;
		this.idProvider = idProvider;
	}

	/**
	 * @param index             {@link #index}
	 * @param restClientBuilder {@link #restClientBuilder}
	 * @param objectMapper      {@link #objectMapper}
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

			createIndexIfNotExists(restClient);

			input.forEach(document -> {
				final String documentJson;
				try {
					documentJson = objectMapper.writeValueAsString(document);
				} catch (final JsonProcessingException jsonProcessingException) {
					throw new IllegalStateException("error creating json for document " + document,
							jsonProcessingException);
				}

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

		// https://github.com/hwchase17/langchain/blob/master/langchain/retrievers/elastic_search_bm25.py
		final ObjectNode indexRequestBody = this.objectMapper.createObjectNode();

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
		final Request indexRequest = new Request("PUT", '/' + index);
		indexRequest.setJsonEntity(indexRequestBodyJson);
		try {
			restClient.performRequest(indexRequest);
		} catch (final IOException ioException) {
			throw new IllegalStateException("error creating index " + indexRequestBodyJson, ioException);
		}
	}
}
