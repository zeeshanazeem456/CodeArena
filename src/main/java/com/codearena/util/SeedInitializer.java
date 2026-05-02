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
import java.sql.Types;

public final class SeedInitializer {

    private static final String SEED_RESOURCE = "data/seed.sql";
    private static final String DEMO_PASSWORD_HASH = "$2a$10$iyHiGiN36A2WlwBTjabeX.o4GjgEb7TQOMCNcx4OjtnswFpNrWBFu";

    private SeedInitializer() {
    }

    public static void run() throws SQLException {
        ensureSeedMetadataTable();
        if (alreadySeeded()) {
            repairKnownSeedData();
            ensureDemoContent();
            return;
        }

        try (InputStream inputStream = SeedInitializer.class.getClassLoader().getResourceAsStream(SEED_RESOURCE)) {
            if (inputStream == null) {
                throw new SQLException("Seed resource not found on classpath: " + SEED_RESOURCE);
            }

            String seedSql = read(inputStream);
            executeInsertStatements(seedSql);
            repairKnownSeedData();
            ensureDemoContent();
            markSeeded();
        } catch (IOException exception) {
            throw new SQLException("Unable to read seed resource: " + SEED_RESOURCE, exception);
        }
    }

    private static void ensureSeedMetadataTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS app_metadata (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
                """;

        Connection connection = DBConnection.getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static boolean alreadySeeded() throws SQLException {
        String sql = "SELECT value FROM app_metadata WHERE key = ?";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "seed.sql.v1");
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void markSeeded() throws SQLException {
        String sql = "INSERT OR REPLACE INTO app_metadata (key, value) VALUES (?, datetime('now'))";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "seed.sql.v1");
            statement.executeUpdate();
        }
    }

    private static String read(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (Reader reader = new InputStreamReader(inputStream)) {
            char[] buffer = new char[2048];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, charsRead);
            }
        }
        return content.toString();
    }

    private static void executeInsertStatements(String seedSql) throws SQLException {
        Connection connection = DBConnection.getConnection();
        for (String statementSql : seedSql.split(";")) {
            String normalized = normalize(statementSql);
            if (!normalized.regionMatches(true, 0, "INSERT", 0, "INSERT".length())) {
                continue;
            }

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(normalized);
            }
        }
    }

    private static void repairKnownSeedData() throws SQLException {
        String sql = """
                UPDATE test_cases
                SET expected = ?
                WHERE problem_id = 3 AND input = ? AND is_sample = 0
                """;

        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "1\\n2\\nFizz\\n4\\nBuzz\\nFizz\\n7\\n8\\nFizz\\nBuzz\\n11\\nFizz\\n13\\n14\\nFizzBuzz");
            statement.setString(2, "15");
            statement.executeUpdate();
        }
    }

    private static void ensureDemoContent() throws SQLException {
        ensureDemoUser("byteknight", "byteknight@codearena.com", 120, "Apprentice", 3, 1, 0);
        ensureDemoUser("loopwizard", "loopwizard@codearena.com", 80, "Novice", 2, 0, 1);
        ensureDemoUser("stackrider", "stackrider@codearena.com", 210, "Specialist", 5, 2, 1);

        int palindromeId = ensureProblem(
                "Palindrome Check",
                "Read a word and print YES if it is a palindrome, otherwise print NO.",
                "Input length is between 1 and 100 characters.",
                "A single lowercase word.",
                "Print YES or NO.",
                "Easy",
                "Strings",
                "string,two-pointer");
        ensureTestCase(palindromeId, "madam", "YES", true, 1);
        ensureTestCase(palindromeId, "arena", "NO", false, 2);

        int maxId = ensureProblem(
                "Maximum in Array",
                "Read N integers and print the largest value.",
                "1 <= N <= 1000. Values fit in a signed 32-bit integer.",
                "First line contains N. Second line contains N integers.",
                "Print the maximum integer.",
                "Medium",
                "Arrays",
                "array,loops");
        ensureTestCase(maxId, "5\n3 9 1 7 2", "9", true, 1);
        ensureTestCase(maxId, "4\n-5 -2 -9 -1", "-1", false, 2);

        int primeId = ensureProblem(
                "Prime Counter",
                "Read N and print how many prime numbers exist from 1 to N inclusive.",
                "1 <= N <= 10000.",
                "A single integer N.",
                "Print the count of prime numbers.",
                "Hard",
                "Math",
                "prime,number-theory");
        ensureTestCase(primeId, "10", "4", true, 1);
        ensureTestCase(primeId, "30", "10", false, 2);
    }

    private static void ensureDemoUser(String username, String email, int xp, String rankTitle,
                                       int problemsSolved, int battlesWon, int battlesLost) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO users
                    (username, email, password, role, xp, rank_title, problems_solved, battles_won, battles_lost)
                VALUES (?, ?, ?, 'CODER', ?, ?, ?, ?, ?)
                """;

        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, DEMO_PASSWORD_HASH);
            statement.setInt(4, xp);
            statement.setString(5, rankTitle);
            statement.setInt(6, problemsSolved);
            statement.setInt(7, battlesWon);
            statement.setInt(8, battlesLost);
            statement.executeUpdate();
        }
    }

    private static int ensureProblem(String title, String description, String constraints, String inputFormat,
                                     String outputFormat, String difficulty, String category, String tags) throws SQLException {
        int existingId = findProblemIdByTitle(title);
        if (existingId != 0) {
            enrichProblem(existingId, constraints, inputFormat, outputFormat);
            return existingId;
        }

        String sql = """
                INSERT INTO problems
                    (title, description, constraints, input_format, output_format, difficulty, category, tags, is_published, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?)
                """;

        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setString(3, constraints);
            statement.setString(4, inputFormat);
            statement.setString(5, outputFormat);
            statement.setString(6, difficulty);
            statement.setString(7, category);
            statement.setString(8, tags);
            int adminId = findUserIdByUsername("admin");
            if (adminId == 0) {
                statement.setNull(9, Types.INTEGER);
            } else {
                statement.setInt(9, adminId);
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : findProblemIdByTitle(title);
            }
        }
    }

    private static void enrichProblem(int problemId, String constraints, String inputFormat, String outputFormat) throws SQLException {
        String sql = """
                UPDATE problems
                SET constraints = CASE WHEN constraints IS NULL OR trim(constraints) = '' THEN ? ELSE constraints END,
                    input_format = CASE WHEN input_format IS NULL OR trim(input_format) = '' THEN ? ELSE input_format END,
                    output_format = CASE WHEN output_format IS NULL OR trim(output_format) = '' THEN ? ELSE output_format END
                WHERE id = ?
                """;

        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, constraints);
            statement.setString(2, inputFormat);
            statement.setString(3, outputFormat);
            statement.setInt(4, problemId);
            statement.executeUpdate();
        }
    }

    private static void ensureTestCase(int problemId, String input, String expected, boolean sample, int sequenceOrder) throws SQLException {
        if (problemId == 0 || testCaseExists(problemId, input)) {
            return;
        }

        String sql = """
                INSERT INTO test_cases (problem_id, input, expected, is_sample, sequence_order)
                VALUES (?, ?, ?, ?, ?)
                """;

        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, problemId);
            statement.setString(2, input);
            statement.setString(3, expected);
            statement.setInt(4, sample ? 1 : 0);
            statement.setInt(5, sequenceOrder);
            statement.executeUpdate();
        }
    }

    private static int findProblemIdByTitle(String title) throws SQLException {
        String sql = "SELECT id FROM problems WHERE title = ? ORDER BY id ASC LIMIT 1";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("id") : 0;
            }
        }
    }

    private static int findUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("id") : 0;
            }
        }
    }

    private static boolean testCaseExists(int problemId, String input) throws SQLException {
        String sql = "SELECT 1 FROM test_cases WHERE problem_id = ? AND input = ? LIMIT 1";
        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, problemId);
            statement.setString(2, input);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static String normalize(String sql) {
        StringBuilder normalized = new StringBuilder();
        for (String line : sql.lines().toList()) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("--")) {
                normalized.append(line).append(System.lineSeparator());
            }
        }
        return normalized.toString().trim();
    }
}
