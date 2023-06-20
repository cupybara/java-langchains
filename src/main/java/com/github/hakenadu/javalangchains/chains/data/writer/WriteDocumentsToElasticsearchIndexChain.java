package com.github.hakenadu.javalangchains.chains.data.writer;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hakenadu.javalangchains.chains.Chain;

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
	 * @param index             {@link #index}
	 * @param restClientBuilder {@link #restClientBuilder}
	 * @param objectMapper      {@link #objectMapper}
	 */
	public WriteDocumentsToElasticsearchIndexChain(final String index, final RestClientBuilder restClientBuilder,
			final ObjectMapper objectMapper) {
		this.index = index;
		this.restClientBuilder = restClientBuilder;
		this.objectMapper = objectMapper;
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

			input.forEach(document -> {
				final String documentJson;
				try {
					documentJson = objectMapper.writeValueAsString(document);
				} catch (final JsonProcessingException jsonProcessingException) {
					throw new IllegalStateException("error creating json for document " + document,
							jsonProcessingException);
				}

				final Request indexRequest = new Request("POST", String.format("/%s/_doc", index));
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
}
