package io.github.cupybara.javalangchains.chains.data.retrieval;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.http.HttpHost;
import org.apache.lucene.search.Query;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.cupybara.javalangchains.util.PromptConstants;

/**
 * This {@link RetrievalChain} retrieves documents from an elasticsearch index
 */
public class ElasticsearchRetrievalChain extends RetrievalChain implements Closeable {

	/**
	 * elasticsearch index name
	 */
	private final String index;

	/**
	 * elasticsearch low level {@link RestClient}
	 */
	private final RestClient restClient;

	/**
	 * this {@link Function} accepts the user's question and provides the
	 * {@link Query} which is executed against the Elasticsearch _search API
	 */
	private final Function<String, ObjectNode> queryCreator;

	/**
	 * Consumes an elasticsearch hit and the question and creates a document as a
	 * result
	 */
	private final BiFunction<ObjectNode, String, Map<String, String>> documentCreator;

	/**
	 * {@link ObjectMapper} used for query creation and document deserialization
	 */
	private final ObjectMapper objectMapper;

	/**
	 * Creates an instance of {@link ElasticsearchRetrievalChain}
	 * 
	 * @param index            {@link #index}
	 * @param restClient       {@link #restClient}
	 * @param maxDocumentCount {@link #getMaxDocumentCount()}
	 * @param objectMapper     {@link #objectMapper}
	 * @param queryCreator     {@link #queryCreator}
	 * @param documentCreator  {@link #documentCreator}
	 */
	public ElasticsearchRetrievalChain(final String index, final RestClient restClient, final int maxDocumentCount,
			final ObjectMapper objectMapper, final Function<String, ObjectNode> queryCreator,
			final BiFunction<ObjectNode, String, Map<String, String>> documentCreator) {
		super(maxDocumentCount);
		this.index = index;
		this.restClient = restClient;
		this.objectMapper = objectMapper;
		this.queryCreator = queryCreator;
		this.documentCreator = documentCreator;
	}

	/**
	 * Creates an instance of {@link ElasticsearchRetrievalChain}
	 * 
	 * @param index            {@link #index}
	 * @param restClient       {@link #restClient}
	 * @param maxDocumentCount {@link #getMaxDocumentCount()}
	 * @param objectMapper     {@link #objectMapper}
	 * @param queryCreator     {@link #queryCreator}
	 */
	public ElasticsearchRetrievalChain(final String index, final RestClient restClient, final int maxDocumentCount,
			final ObjectMapper objectMapper, final Function<String, ObjectNode> queryCreator) {
		this(index, restClient, maxDocumentCount, objectMapper, queryCreator, defaultDocumentCreator(objectMapper));
	}

	/**
	 * Creates an instance of {@link ElasticsearchRetrievalChain}
	 * 
	 * @param index            {@link #index}
	 * @param restClient       {@link #restClient}
	 * @param maxDocumentCount {@link #getMaxDocumentCount}
	 * @param objectMapper     {@link #objectMapper}
	 */
	public ElasticsearchRetrievalChain(final String index, final RestClient restClient, final int maxDocumentCount,
			final ObjectMapper objectMapper) {
		this(index, restClient, maxDocumentCount, objectMapper, question -> createQuery(objectMapper, question));
	}

	/**
	 * Creates an instance of {@link ElasticsearchRetrievalChain}
	 * 
	 * @param index            {@link #index}
	 * @param restClient       {@link #restClient}
	 * @param maxDocumentCount {@link #getMaxDocumentCount}
	 */
	public ElasticsearchRetrievalChain(final String index, final RestClient restClient, final int maxDocumentCount) {
		this(index, restClient, maxDocumentCount, new ObjectMapper());
	}

	/**
	 * Creates an instance of {@link ElasticsearchRetrievalChain}
	 * 
	 * @param index      {@link #index}
	 * @param restClient {@link #restClient}
	 */
	public ElasticsearchRetrievalChain(final String index, final RestClient restClient) {
		this(index, restClient, 4);
	}

	/**
	 * Creates an instance of {@link ElasticsearchRetrievalChain}
	 * 
	 * @param index {@link #index}
	 */
	public ElasticsearchRetrievalChain(final String index) {
		this(index, RestClient.builder(new HttpHost("localhost", 9200)).build());
	}

	@Override
	public Stream<Map<String, String>> run(final String input) {
		final ObjectNode query = queryCreator.apply(input);

		final String requestJson = objectMapper.createObjectNode().put("size", getMaxDocumentCount())
				.set("query", query).toString();

		final Request searchRequest = new Request("GET", String.format("/%s/_search", index));
		searchRequest.setJsonEntity(requestJson);

		final Response searchResponse;
		try {
			searchResponse = restClient.performRequest(searchRequest);
		} catch (final IOException ioException) {
			throw new IllegalStateException("error executing search with request " + requestJson, ioException);
		}

		final ObjectNode response;
		try (final InputStream responseInputStream = searchResponse.getEntity().getContent()) {
			response = (ObjectNode) objectMapper.readTree(responseInputStream);
		} catch (final IOException ioException) {
			throw new IllegalStateException("error parsing search response", ioException);
		}

		final ArrayNode hits = Optional.of(response).map(o -> o.get("hits")).map(ObjectNode.class::cast)
				.map(o -> o.get("hits")).map(ArrayNode.class::cast).orElse(null);

		if (hits == null) {
			return Stream.empty();
		}

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(hits.iterator(), Spliterator.ORDERED), false)
				.map(ObjectNode.class::cast).map(hitNode -> documentCreator.apply(hitNode, input));
	}

	@Override
	public void close() throws IOException {
		this.restClient.close();
	}

	/**
	 * @param objectMapper {@link ObjectMapper} used for {@link ObjectNode} creation
	 * @param question     the question used for retrieval
	 * @return {"match": {"content": question}}
	 */
	private static ObjectNode createQuery(final ObjectMapper objectMapper, final String question) {
		final ObjectNode query = objectMapper.createObjectNode();
		query.putObject("match").put(PromptConstants.CONTENT, question);
		return query;
	}

	/**
	 * creates the default {@link #queryCreator}
	 * 
	 * @param objectMapper the {@link ObjectMapper} used for json operations
	 * @return {@link BiFunction} which consumes a hit node and the question and
	 *         produces a document consisting of all (key, value)-pairs of the hit's
	 *         _source object
	 */
	public static BiFunction<ObjectNode, String, Map<String, String>> defaultDocumentCreator(
			final ObjectMapper objectMapper) {
		return (hitObjectNode, question) -> {
			final ObjectNode source = (ObjectNode) hitObjectNode.get("_source");

			final Map<String, Object> sourceMap = objectMapper.convertValue(source,
					new TypeReference<Map<String, Object>>() {
						// noop
					});

			final Map<String, String> document = new HashMap<>();
			document.put(PromptConstants.QUESTION, question);

			for (final Entry<String, Object> sourceEntry : sourceMap.entrySet()) {
				document.put(sourceEntry.getKey(), sourceEntry.getValue().toString());
			}

			return document;
		};
	}
}
