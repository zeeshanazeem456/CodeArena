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

    private static final Path DB_PATH = resolveDatabasePath();
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
        Path directory = DB_PATH.getParent();
        if (directory == null) {
            return;
        }
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new SQLException("Unable to create database directory: " + directory, exception);
        }
    }

    private static Path resolveDatabasePath() {
        String propertyPath = System.getProperty("codearena.db.path");
        if (propertyPath != null && !propertyPath.trim().isEmpty()) {
            return Paths.get(propertyPath.trim()).toAbsolutePath().normalize();
        }

        String environmentPath = System.getenv("CODEARENA_DB_PATH");
        if (environmentPath != null && !environmentPath.trim().isEmpty()) {
            return Paths.get(environmentPath.trim()).toAbsolutePath().normalize();
        }

        return Paths.get(System.getProperty("user.dir"), "data", "codearena.db").toAbsolutePath().normalize();
    }

    private static void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }
}
