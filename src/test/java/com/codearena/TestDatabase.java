package com.codearena;

import com.codearena.util.DBConnection;
import com.codearena.util.PersistenceHandler;
import com.codearena.util.SessionManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class TestDatabase {

    private static final Path TEST_DB = Path.of("target", "test-data", "codearena-test.db")
            .toAbsolutePath()
            .normalize();

    private TestDatabase() {
    }

    public static void reset() throws Exception {
        System.setProperty("codearena.db.path", TEST_DB.toString());
        SessionManager.clearSession();
        DBConnection.closeConnection();
        deleteIfExists(TEST_DB);
        PersistenceHandler.initialize();
    }

    public static int countRows(String tableName) throws SQLException {
        try (PreparedStatement statement = connection().prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    public static int findProblemId(String title) throws SQLException {
        String sql = "SELECT id FROM problems WHERE title = ?";
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, title);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public static boolean userHasBadge(int userId, String badgeCode) throws SQLException {
        String sql = """
                SELECT 1
                FROM user_badges ub
                JOIN badges b ON b.id = ub.badge_id
                WHERE ub.user_id = ? AND b.code = ?
                """;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, badgeCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static Connection connection() throws SQLException {
        return DBConnection.getConnection();
    }

    private static void deleteIfExists(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);
    }
}
