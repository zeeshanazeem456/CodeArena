package com.codearena.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    private static final String SCHEMA_RESOURCE = "data/schema.sql";

    private SchemaInitializer() {
    }

    public static void run() throws SQLException {
        try (InputStream inputStream = SchemaInitializer.class.getClassLoader().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (inputStream == null) {
                throw new SQLException("Schema resource not found on classpath: " + SCHEMA_RESOURCE);
            }

            String schemaSql = readSchema(inputStream);
            executeCreateTableStatements(schemaSql);
        } catch (IOException exception) {
            throw new SQLException("Unable to read schema resource: " + SCHEMA_RESOURCE, exception);
        }
    }

    private static String readSchema(InputStream inputStream) throws IOException {
        StringBuilder schema = new StringBuilder();
        try (Reader reader = new InputStreamReader(inputStream)) {
            char[] buffer = new char[2048];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                schema.append(buffer, 0, charsRead);
            }
        }
        return schema.toString();
    }

    private static void executeCreateTableStatements(String schemaSql) throws SQLException {
        Connection connection = DBConnection.getConnection();
        for (String statementSql : schemaSql.split(";")) {
            String normalizedStatement = normalizeStatement(statementSql);
            if (!normalizedStatement.regionMatches(true, 0, "CREATE TABLE", 0, "CREATE TABLE".length())) {
                continue;
            }

            String tableName = extractTableName(normalizedStatement);
            if (tableName == null || tableExists(connection, tableName)) {
                continue;
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute(normalizedStatement);
            }
        }
    }

    private static String normalizeStatement(String sql) {
        StringBuilder normalized = new StringBuilder();
        for (String line : sql.lines().toList()) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("--")) {
                normalized.append(line).append(System.lineSeparator());
            }
        }
        return normalized.toString().trim();
    }

    private static String extractTableName(String createTableStatement) {
        String compactSql = createTableStatement.replace('\r', ' ').replace('\n', ' ').trim();
        String[] tokens = compactSql.split("\\s+");

        for (int index = 0; index < tokens.length - 1; index++) {
            if ("TABLE".equalsIgnoreCase(tokens[index])) {
                int nameIndex = index + 1;
                while (nameIndex < tokens.length
                        && ("IF".equalsIgnoreCase(tokens[nameIndex])
                        || "NOT".equalsIgnoreCase(tokens[nameIndex])
                        || "EXISTS".equalsIgnoreCase(tokens[nameIndex]))) {
                    nameIndex++;
                }

                if (nameIndex < tokens.length) {
                    return tokens[nameIndex].replace("\"", "").replace("`", "").replace("[", "").replace("]", "");
                }
            }
        }

        return null;
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
