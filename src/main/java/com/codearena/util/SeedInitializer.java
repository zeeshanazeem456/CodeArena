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
        ensureBadges();
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

        ensureProblemWithCases(
                "Array Sum",
                "Read N integers and print their sum.",
                "1 <= N <= 1000. Values fit in a signed 32-bit integer.",
                "First line contains N. Second line contains N integers.",
                "Print the sum of all integers.",
                "Easy",
                "Arrays",
                "array,loops,sum",
                new SeedTestCase("5\n1 2 3 4 5", "15", true, 1),
                new SeedTestCase("4\n-3 10 5 -2", "10", false, 2));

        ensureProblemWithCases(
                "Even or Odd",
                "Read an integer and print Even if it is even, otherwise print Odd.",
                "-100000 <= N <= 100000.",
                "A single integer N.",
                "Print Even or Odd.",
                "Easy",
                "Basics",
                "conditionals,math",
                new SeedTestCase("8", "Even", true, 1),
                new SeedTestCase("-7", "Odd", false, 2));

        ensureProblemWithCases(
                "Sum of Digits",
                "Read a non-negative integer and print the sum of its digits.",
                "0 <= N <= 1000000000.",
                "A single integer N.",
                "Print the digit sum.",
                "Easy",
                "Math",
                "digits,loops",
                new SeedTestCase("12345", "15", true, 1),
                new SeedTestCase("900001", "10", false, 2));

        ensureProblemWithCases(
                "Count Vowels",
                "Read a lowercase word and print how many vowels it contains.",
                "Input length is between 1 and 100 characters.",
                "A single lowercase word.",
                "Print the number of vowels.",
                "Easy",
                "Strings",
                "string,counting",
                new SeedTestCase("education", "5", true, 1),
                new SeedTestCase("rhythm", "0", false, 2));

        ensureProblemWithCases(
                "Factorial",
                "Read N and print N factorial.",
                "0 <= N <= 12.",
                "A single integer N.",
                "Print N factorial.",
                "Easy",
                "Math",
                "loops,recursion",
                new SeedTestCase("5", "120", true, 1),
                new SeedTestCase("0", "1", false, 2));

        ensureProblemWithCases(
                "Second Largest",
                "Read N distinct integers and print the second largest value.",
                "2 <= N <= 1000. All values are distinct.",
                "First line contains N. Second line contains N integers.",
                "Print the second largest integer.",
                "Medium",
                "Arrays",
                "array,selection",
                new SeedTestCase("5\n10 4 8 15 2", "10", true, 1),
                new SeedTestCase("4\n-8 -3 -10 -1", "-3", false, 2));

        ensureProblemWithCases(
                "Binary Search",
                "Read a sorted array and a target. Print the index of the target, or -1 if it is missing.",
                "1 <= N <= 1000. Array is sorted in ascending order.",
                "First line contains N. Second line contains N integers. Third line contains target.",
                "Print the zero-based index or -1.",
                "Medium",
                "Searching",
                "binary-search,array",
                new SeedTestCase("5\n1 3 5 7 9\n7", "3", true, 1),
                new SeedTestCase("6\n2 4 6 8 10 12\n5", "-1", false, 2));

        ensureProblemWithCases(
                "GCD and LCM",
                "Read two positive integers and print their GCD and LCM.",
                "1 <= A, B <= 100000.",
                "Two integers A and B.",
                "Print GCD and LCM separated by a space.",
                "Medium",
                "Math",
                "gcd,lcm,euclid",
                new SeedTestCase("12 18", "6 36", true, 1),
                new SeedTestCase("21 6", "3 42", false, 2));

        ensureProblemWithCases(
                "Balanced Parentheses",
                "Read a string containing only parentheses and print YES if it is balanced, otherwise print NO.",
                "Input length is between 1 and 1000.",
                "A single string of '(' and ')' characters.",
                "Print YES or NO.",
                "Medium",
                "Stacks",
                "stack,string,parentheses",
                new SeedTestCase("(()())", "YES", true, 1),
                new SeedTestCase("())(()", "NO", false, 2));

        ensureProblemWithCases(
                "Missing Number",
                "Numbers from 1 to N are given with one missing value. Print the missing value.",
                "2 <= N <= 100000.",
                "First line contains N. Second line contains N - 1 integers.",
                "Print the missing number.",
                "Medium",
                "Arrays",
                "array,math",
                new SeedTestCase("5\n1 2 4 5", "3", true, 1),
                new SeedTestCase("7\n7 3 1 2 5 6", "4", false, 2));

        ensureProblemWithCases(
                "Rotate Array",
                "Read an array and K. Rotate the array to the right by K positions and print it.",
                "1 <= N <= 1000. 0 <= K <= 100000.",
                "First line contains N. Second line contains N integers. Third line contains K.",
                "Print the rotated array values separated by spaces.",
                "Medium",
                "Arrays",
                "array,rotation",
                new SeedTestCase("5\n1 2 3 4 5\n2", "4 5 1 2 3", true, 1),
                new SeedTestCase("4\n10 20 30 40\n5", "40 10 20 30", false, 2));

        ensureProblemWithCases(
                "Matrix Diagonal Sum",
                "Read an N by N matrix and print the sum of both diagonals. Count the center only once.",
                "1 <= N <= 50.",
                "First line contains N, followed by N lines with N integers each.",
                "Print the diagonal sum.",
                "Medium",
                "Matrices",
                "matrix,loops",
                new SeedTestCase("3\n1 2 3\n4 5 6\n7 8 9", "25", true, 1),
                new SeedTestCase("2\n1 2\n3 4", "10", false, 2));

        ensureProblemWithCases(
                "Unique Elements",
                "Read N integers and print the values that appear exactly once, preserving their original order.",
                "1 <= N <= 1000.",
                "First line contains N. Second line contains N integers.",
                "Print unique values separated by spaces. Print NONE if there are no unique values.",
                "Medium",
                "Arrays",
                "hashmap,array,counting",
                new SeedTestCase("7\n4 5 4 6 7 5 8", "6 7 8", true, 1),
                new SeedTestCase("4\n2 2 3 3", "NONE", false, 2));

        ensureProblemWithCases(
                "Longest Word",
                "Read N words and print the longest word. If there is a tie, print the first one.",
                "1 <= N <= 100. Each word contains lowercase letters only.",
                "First line contains N. Next N tokens are words.",
                "Print the longest word.",
                "Medium",
                "Strings",
                "string,scan",
                new SeedTestCase("4\njava arena code tournament", "tournament", true, 1),
                new SeedTestCase("3\nalpha beta gamma", "alpha", false, 2));

        ensureProblemWithCases(
                "Power Mod",
                "Read A, B, and M. Print A raised to B modulo M.",
                "1 <= A, B <= 1000000000. 2 <= M <= 1000000000.",
                "Three integers A, B, and M.",
                "Print the modular power result.",
                "Hard",
                "Math",
                "fast-power,modulo",
                new SeedTestCase("2 10 1000", "24", true, 1),
                new SeedTestCase("7 13 100", "7", false, 2));

        ensureProblemWithCases(
                "Longest Increasing Subsequence",
                "Read N integers and print the length of the longest strictly increasing subsequence.",
                "1 <= N <= 1000.",
                "First line contains N. Second line contains N integers.",
                "Print the LIS length.",
                "Hard",
                "Dynamic Programming",
                "dp,array,lis",
                new SeedTestCase("6\n10 9 2 5 3 7", "3", true, 1),
                new SeedTestCase("8\n1 3 6 7 9 4 10 5", "6", false, 2));

        ensureProblemWithCases(
                "Coin Change Ways",
                "Read coin denominations and a target amount. Print how many ways can form the amount using unlimited coins.",
                "1 <= N <= 20. 0 <= amount <= 1000.",
                "First line contains N. Second line contains N coin values. Third line contains amount.",
                "Print the number of ways.",
                "Hard",
                "Dynamic Programming",
                "dp,coin-change",
                new SeedTestCase("3\n1 2 5\n5", "4", true, 1),
                new SeedTestCase("4\n2 3 5 6\n10", "5", false, 2));

        ensureProblemWithCases(
                "Shortest Path in Grid",
                "Read a grid with open cells and blocked cells. Move up, down, left, or right from S to E and print the shortest distance.",
                "1 <= R, C <= 30. Grid contains one S and one E. Dot means open and hash means blocked.",
                "First line contains R and C, followed by R grid rows.",
                "Print the shortest distance, or -1 if E cannot be reached.",
                "Hard",
                "Graphs",
                "bfs,grid,queue",
                new SeedTestCase("3 4\nS...\n.##.\n...E", "5", true, 1),
                new SeedTestCase("3 3\nS#.\n###\n..E", "-1", false, 2));

        ensureProblemWithCases(
                "Connected Components",
                "Read an undirected graph and print the number of connected components.",
                "1 <= N <= 1000. 0 <= M <= 5000.",
                "First line contains N and M. Next M lines contain edges U V using 1-based nodes.",
                "Print the number of connected components.",
                "Hard",
                "Graphs",
                "dfs,bfs,graph",
                new SeedTestCase("5 3\n1 2\n2 3\n4 5", "2", true, 1),
                new SeedTestCase("4 0", "4", false, 2));
    }

    private static void ensureBadges() throws SQLException {
        ensureBadge("FIRST_SOLVE", "First Solve", "Solve your first programming problem.", "Problems",
                "/images/badges/first_solve.png", 10);
        ensureBadge("SOLVE_5", "Warm Up", "Solve 5 programming problems.", "Problems",
                "/images/badges/solve_5.png", 20);
        ensureBadge("SOLVE_25", "Problem Hunter", "Solve 25 programming problems.", "Problems",
                "/images/badges/solve_25.png", 30);
        ensureBadge("FIRST_HARD", "Hard Mode", "Solve your first Hard problem.", "Problems",
                "/images/badges/first_hard.png", 40);
        ensureBadge("JAVA_SOLVER", "Java Solver", "Get an accepted solution in Java.", "Languages",
                "/images/badges/java_solver.png", 50);
        ensureBadge("PYTHON_SOLVER", "Python Solver", "Get an accepted solution in Python.", "Languages",
                "/images/badges/python_solver.png", 60);
        ensureBadge("FIRST_BATTLE", "First Duel", "Complete your first battle.", "Battles",
                "/images/badges/first_battle.png", 70);
        ensureBadge("FIRST_BATTLE_WIN", "Arena Rookie", "Win your first battle.", "Battles",
                "/images/badges/first_battle_win.png", 80);
        ensureBadge("BATTLE_WINS_5", "Duelist", "Win 5 battles.", "Battles",
                "/images/badges/battle_wins_5.png", 90);
        ensureBadge("RANDOM_BATTLES_10", "Random Warrior", "Complete 10 random 1v1 battles.", "Battles",
                "/images/badges/random_battles_10.png", 100);
        ensureBadge("STREAK_3", "3 Day Spark", "Build a 3 day solving streak.", "Consistency",
                "/images/badges/streak_3.png", 110);
        ensureBadge("STREAK_7", "7 Day Flame", "Build a 7 day solving streak.", "Consistency",
                "/images/badges/streak_7.png", 120);
    }

    private static void ensureBadge(String code, String name, String description, String category,
                                    String imagePath, int sortOrder) throws SQLException {
        String sql = """
                INSERT INTO badges (code, name, description, category, image_path, sort_order, is_active)
                VALUES (?, ?, ?, ?, ?, ?, 1)
                ON CONFLICT(code) DO UPDATE SET
                    name = excluded.name,
                    description = excluded.description,
                    category = excluded.category,
                    image_path = excluded.image_path,
                    sort_order = excluded.sort_order,
                    is_active = 1
                """;

        Connection connection = DBConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setString(4, category);
            statement.setString(5, imagePath);
            statement.setInt(6, sortOrder);
            statement.executeUpdate();
        }
    }

    private static void ensureProblemWithCases(String title, String description, String constraints,
                                               String inputFormat, String outputFormat, String difficulty,
                                               String category, String tags, SeedTestCase... testCases) throws SQLException {
        int problemId = ensureProblem(title, description, constraints, inputFormat, outputFormat, difficulty, category, tags);
        for (SeedTestCase testCase : testCases) {
            ensureTestCase(problemId, testCase.input(), testCase.expected(), testCase.sample(), testCase.sequenceOrder());
        }
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

    private record SeedTestCase(String input, String expected, boolean sample, int sequenceOrder) {
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
