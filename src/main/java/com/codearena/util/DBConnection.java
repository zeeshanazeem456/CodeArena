package com.codearena.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBConnection {

    private static final Path DB_DIRECTORY = Paths.get(System.getProperty("user.home"), ".codearena");
    private static final Path DB_PATH = DB_DIRECTORY.resolve("codearena.db");
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    private static Connection connection;

    private DBConnection() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        ensureDatabaseDirectory();

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            enableForeignKeys(connection);
        }

        return connection;
    }

    public static Path getDatabasePath() {
        return DB_PATH;
    }

    public static synchronized void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private static void ensureDatabaseDirectory() throws SQLException {
        try {
            Files.createDirectories(DB_DIRECTORY);
        } catch (IOException exception) {
            throw new SQLException("Unable to create database directory: " + DB_DIRECTORY, exception);
        }
    }

    private static void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }
}
