package io.github.cupybara.javalangchains.chains.data.retrieval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.cupybara.javalangchains.util.PromptConstants;

@Disabled
public class JdbcRetrievalChainIT {
	private static Supplier<Connection> connectionSupplier;

	@BeforeAll
	static void setup() throws SQLException {
		final String connectionString = "jdbc:postgresql://localhost:5432/";
		final String username = "postgres";
		final String password = "admin";

		Connection connection = DriverManager.getConnection(connectionString, username, password);

		Statement setupStatement = connection.createStatement();

		ResultSet dbResult = setupStatement
				.executeQuery("SELECT datname FROM pg_catalog.pg_database WHERE datname='langchain_test'");
		if (dbResult.next()) {
			setupStatement.execute("DROP DATABASE langchain_test");
		}

		setupStatement.execute("CREATE DATABASE langchain_test");

		setupStatement.close();

		connection.setCatalog("langchain_test");

		Statement createTableStatement = connection.createStatement();
		if (connection.getMetaData().getTables("langchain_test", null, null, new String[] { "TABLE" }).next()) {
			createTableStatement.execute("DROP TABLE Documents");
		}
		createTableStatement.execute(
				"CREATE TABLE Documents (source VARCHAR PRIMARY KEY, content VARCHAR, additional_attribute INTEGER)");
		createTableStatement.close();
		for (int i = 0; i < DocumentTestUtil.DOCUMENTS.size(); i++) {
			String content = DocumentTestUtil.DOCUMENTS.get(i);
			PreparedStatement seedStatement = connection
					.prepareStatement("INSERT INTO Documents(source, content, additional_attribute) VALUES (?, ?, 1)");
			seedStatement.setString(1, Integer.toString(i));
			seedStatement.setString(2, content);
			seedStatement.execute();
			seedStatement.close();
		}

		connectionSupplier = () -> connection;
	}

	@Test
	void testRun() throws SQLException {
		JdbcRetrievalChain jdbcRetrievalChain = new JdbcRetrievalChain(connectionSupplier, 1);

		final List<Map<String, String>> retrievedDocuments = jdbcRetrievalChain.run("who is john doe?")
				.collect(Collectors.toList());
		assertEquals(1, retrievedDocuments.size(), "incorrect number of retrieved documents");

		Map<String, String> document = retrievedDocuments.get(0);
		assertEquals("0", document.get("source"));
		assertEquals("1", document.get("additional_attribute"));
		assertEquals(DocumentTestUtil.DOCUMENT_1, document.get(PromptConstants.CONTENT));
		assertEquals("who is john doe?", document.get(PromptConstants.QUESTION));
	}
}
