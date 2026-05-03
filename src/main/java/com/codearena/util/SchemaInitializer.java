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
            ensureSchemaMigrations();
        } catch (IOException exception) {
            throw new SQLException("Unable to read schema resource: " + SCHEMA_RESOURCE, exception);
        }
    }

    private static void ensureSchemaMigrations() throws SQLException {
        Connection connection = DBConnection.getConnection();
        if (!columnExists(connection, "test_cases", "sequence_order")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE test_cases ADD COLUMN sequence_order INTEGER NOT NULL DEFAULT 0");
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute("UPDATE test_cases SET sequence_order = id WHERE sequence_order = 0");
            }
        }
        addTextColumnIfMissing(connection, "problems", "constraints");
        addTextColumnIfMissing(connection, "problems", "input_format");
        addTextColumnIfMissing(connection, "problems", "output_format");
        addTextColumnIfMissing(connection, "battles", "join_code");
        addTextColumnIfMissing(connection, "battles", "battle_mode");
        createBattleParticipantsIfMissing(connection);
        addTextColumnIfMissing(connection, "battle_participants", "ready_at");
        createBadgesIfMissing(connection);
        createUserBadgesIfMissing(connection);
    }

    private static void addTextColumnIfMissing(Connection connection, String tableName, String columnName) throws SQLException {
        if (columnExists(connection, tableName, columnName)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " TEXT");
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

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }

    private static void createBattleParticipantsIfMissing(Connection connection) throws SQLException {
        if (tableExists(connection, "battle_participants")) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS battle_participants (
                        battle_id INTEGER NOT NULL REFERENCES battles(id) ON DELETE CASCADE,
                        user_id   INTEGER NOT NULL REFERENCES users(id),
                        joined_at TEXT    NOT NULL DEFAULT (datetime('now')),
                        ready_at  TEXT,
                        PRIMARY KEY (battle_id, user_id)
                    )
                    """);
        }
    }

    private static void createBadgesIfMissing(Connection connection) throws SQLException {
        if (tableExists(connection, "badges")) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS badges (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        code        TEXT    NOT NULL UNIQUE,
                        name        TEXT    NOT NULL,
                        description TEXT    NOT NULL,
                        category    TEXT    NOT NULL,
                        image_path  TEXT,
                        sort_order  INTEGER NOT NULL DEFAULT 0,
                        is_active   INTEGER NOT NULL DEFAULT 1,
                        created_at  TEXT    NOT NULL DEFAULT (datetime('now'))
                    )
                    """);
        }
    }

    private static void createUserBadgesIfMissing(Connection connection) throws SQLException {
        if (tableExists(connection, "user_badges")) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS user_badges (
                        user_id   INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        badge_id  INTEGER NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
                        earned_at TEXT    NOT NULL DEFAULT (datetime('now')),
                        PRIMARY KEY (user_id, badge_id)
                    )
                    """);
        }
    }
}
