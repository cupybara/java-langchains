package com.github.hakenadu.javalangchains.chains.data.retrieval;

import com.github.hakenadu.javalangchains.util.PromptConstants;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
     * column name for the document content in the JDBC {@link ResultSet}
     */
    private final String contentColumn;
    /**
     * column name for the document source in the JDBC {@link ResultSet}
     */
    private final String sourceColumn;

    /**
     * Creates an instance of {@link JdbcRetrievalChain}
     *
     * @param connectionSupplier {@link  #connectionSupplier}
     * @param queryBuilder       {@link #queryBuilder}
     * @param sourceColumn       {@link #sourceColumn}
     * @param contentColumn      {@link #contentColumn}
     * @param maxDocumentCount   {@link RetrievalChain#getMaxDocumentCount()}
     */
    public JdbcRetrievalChain(Supplier<Connection> connectionSupplier, Function<String, Pair<String, List<Object>>> queryBuilder, String sourceColumn, String contentColumn, int maxDocumentCount) {
        super(maxDocumentCount);
        this.connectionSupplier = connectionSupplier;
        this.queryBuilder = queryBuilder;
        this.sourceColumn = sourceColumn;
        this.contentColumn = contentColumn;
    }

    /**
     * Creates an instance of {@link JdbcRetrievalChain} using {@link #createQuery(String, String, String, String)}
     * for SQL statement creation.
     *
     * @param connectionSupplier {@link  #connectionSupplier}
     * @param table              Name of the document table used for query creation
     * @param sourceColumn       {@link #sourceColumn}
     * @param contentColumn      {@link #contentColumn}
     * @param maxDocumentCount   {@link RetrievalChain#getMaxDocumentCount()}
     */
    public JdbcRetrievalChain(Supplier<Connection> connectionSupplier, String table, String sourceColumn, String contentColumn, int maxDocumentCount) {
        this(connectionSupplier, (question) -> createQuery(question, table, sourceColumn, contentColumn), sourceColumn, contentColumn, maxDocumentCount);
    }

    /**
     * Creates an instance of {@link JdbcRetrievalChain} using {@link #createQuery(String, String, String, String)}
     * for SQL statement creation and `content`, `source` as the result columns and `Documents` as the table.
     *
     * @param connectionSupplier {@link  #connectionSupplier}
     * @param maxDocumentCount   {@link RetrievalChain#getMaxDocumentCount()}
     */
    public JdbcRetrievalChain(Supplier<Connection> connectionSupplier, int maxDocumentCount) {
        this(connectionSupplier, "Documents", "source", "content", maxDocumentCount);
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
                Map<String, String> documentMap = documentFromResultSet(resultSet, input);
                queryResult.add(documentMap);
            }
            return queryResult.stream();
        } catch (SQLException e) {
            throw new IllegalStateException("error creating / executing database statement", e);
        }
    }

    /**
     * Transforms a {@link ResultSet} entry to a document containing the corresponding prompt info.
     * @param resultSet JDBC {@link ResultSet}
     * @param input the user's input
     * @return transformed document map
     * @throws SQLException if a column cannot be retrieved from the result set
     */
    private Map<String, String> documentFromResultSet(ResultSet resultSet, String input) throws SQLException {
        String source = resultSet.getString(sourceColumn);
        String content = resultSet.getString(contentColumn);
        Map<String, String> documentMap = new HashMap<>();
        documentMap.put(PromptConstants.QUESTION, input);
        documentMap.put(PromptConstants.CONTENT, content);
        documentMap.put(PromptConstants.SOURCE, source);
        return documentMap;
    }

    /**
     * Internal query creator that acts as a default when the user doesn't supply a customized function.
     * Creates a SQL statement using a content likeness query.
     *
     * @param question      Input / question of the user
     * @param sourceColumn  Name of the column containing the document source / it's name
     * @param contentColumn Name of the column containing the document content
     * @return a {@link Pair} of the SQL and parameters to bind
     */
    private static Pair<String, List<Object>> createQuery(final String question, final String table, final String sourceColumn, final String contentColumn) {
        final String query = String.format("SELECT %s, %s FROM %s WHERE %s LIKE ANY (?)", sourceColumn, contentColumn, table, contentColumn);
        final String[] splitQuestion = Arrays.stream(question.split(question)).map(t -> String.format("%%%s%%", t)).toArray(String[]::new);
        final List<Object> params = Collections.singletonList(splitQuestion);
        return Pair.of(query, params);
    }
}
