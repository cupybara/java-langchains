package io.github.cupybara.javalangchains.chains.data.retrieval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import io.github.cupybara.javalangchains.util.PromptConstants;

public class JdbcRetrievalChain extends RetrievalChain {
    /**
     * supplier for lazy connection creation on chain invocation
     */
    private final Supplier<Connection> connectionSupplier;
    /**
     * this {@link Function} accepts the user's question and provides the
     * corresponding SQL statement to execute
     */
    private final Function<String, Pair<String, List<Object>>> queryBuilder;
    /**
     * transforms a {@link ResultSet} to a document. default implementation in {@link #documentFromResultSet(ResultSet)}
     */
    private final DocumentCreator documentCreator;

    /**
     * Creates an instance of {@link JdbcRetrievalChain}
     *
     * @param connectionSupplier {@link  #connectionSupplier}
     * @param documentCreator    {@link #documentCreator}
     * @param queryBuilder       {@link #queryBuilder}
     * @param maxDocumentCount   {@link RetrievalChain#getMaxDocumentCount()}
     */
    public JdbcRetrievalChain(Supplier<Connection> connectionSupplier, Function<String, Pair<String, List<Object>>> queryBuilder, DocumentCreator documentCreator, int maxDocumentCount) {
        super(maxDocumentCount);
        this.connectionSupplier = connectionSupplier;
        this.documentCreator = documentCreator;
        this.queryBuilder = queryBuilder;
    }

    /**
     * Creates an instance of {@link JdbcRetrievalChain} using {@link #createQuery(String, String, String)}
     * for SQL statement creation.
     *
     * @param connectionSupplier {@link  #connectionSupplier}
     * @param table              Name of the document table used for query creation
     * @param maxDocumentCount   {@link RetrievalChain#getMaxDocumentCount()}
     */
    public JdbcRetrievalChain(Supplier<Connection> connectionSupplier, String table, String contentColumn, int maxDocumentCount) {
        this(connectionSupplier, (question) -> createQuery(question, table, contentColumn), JdbcRetrievalChain::documentFromResultSet, maxDocumentCount);
    }

    /**
     * Creates an instance of {@link JdbcRetrievalChain} using {@link #createQuery(String, String, String)}
     * for SQL statement creation and `content`, `source` as the result columns and `Documents` as the table.
     *
     * @param connectionSupplier {@link  #connectionSupplier}
     * @param maxDocumentCount   {@link RetrievalChain#getMaxDocumentCount()}
     */
    public JdbcRetrievalChain(Supplier<Connection> connectionSupplier, int maxDocumentCount) {
        this(connectionSupplier, "Documents", "content", maxDocumentCount);
    }

    @Override
    public Stream<Map<String, String>> run(String input) {
        Connection connection = connectionSupplier.get();

        Pair<String, List<Object>> query = queryBuilder.apply(input);
        final String sql = query.getLeft();
        final List<Object> params = query.getRight();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setMaxRows(getMaxDocumentCount());
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            List<Map<String, String>> queryResult = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, String> documentMap = documentCreator.create(resultSet);
                documentMap.put(PromptConstants.QUESTION, input);
                queryResult.add(documentMap);
            }
            return queryResult.stream();
        } catch (SQLException e) {
            throw new IllegalStateException("error creating / executing database statement", e);
        }
    }

    /**
     * Transforms a {@link ResultSet} entry to a document containing the corresponding prompt info.
     *
     * @param resultSet JDBC {@link ResultSet}
     * @return transformed document map
     * @throws SQLException if a column cannot be retrieved from the result set
     */
    private static Map<String, String> documentFromResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        Map<String, String> documentMap = new HashMap<>();

        for(int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            Object value = resultSet.getObject(i);
            documentMap.put(columnName, value.toString());
        }

        return documentMap;
    }

    /**
     * Internal query creator that acts as a default when the user doesn't supply a customized function.
     * Creates a SQL statement using a content likeness query.
     *
     * @param question      Input / question of the user
     * @param contentColumn Name of the column containing the document content
     * @return a {@link Pair} of the SQL and parameters to bind
     */
    private static Pair<String, List<Object>> createQuery(final String question, final String table, final String contentColumn) {
        final String query = String.format("SELECT * FROM %s WHERE %s LIKE ANY (?)", table, contentColumn);
        final String[] splitQuestion = Arrays.stream(question.split(question)).map(t -> String.format("%%%s%%", t)).toArray(String[]::new);
        final List<Object> params = Collections.singletonList(splitQuestion);
        return Pair.of(query, params);
    }

    /**
     * Wrapper interface for Lambdas that act as document creators for a JDBC {@link ResultSet}.
     * Advancing the {@link ResultSet} is not necessary as it is done by the {@link JdbcRetrievalChain}.
     */
    @FunctionalInterface
    public interface DocumentCreator {
        Map<String, String> create(final ResultSet resultSet) throws SQLException;
    }
}
