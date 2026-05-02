package com.codearena.dao;

import com.codearena.model.TestCase;
import com.codearena.util.DAOException;
import com.codearena.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class TestCaseDAO {

    public List<TestCase> getByProblemId(int problemId) {
        String sql = """
                SELECT id, problem_id, input, expected, is_sample, sequence_order
                FROM test_cases
                WHERE problem_id = ?
                ORDER BY sequence_order ASC, id ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, problemId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapTestCases(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load test cases.", exception);
        }
    }

    public List<TestCase> getSampleByProblemId(int problemId) {
        String sql = """
                SELECT id, problem_id, input, expected, is_sample, sequence_order
                FROM test_cases
                WHERE problem_id = ? AND is_sample = 1
                ORDER BY sequence_order ASC, id ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, problemId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapTestCases(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load sample test cases.", exception);
        }
    }

    public void save(TestCase testCase) {
        if (testCase.getId() == 0) {
            insert(testCase);
        } else {
            update(testCase);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM test_cases WHERE id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to delete test case.", exception);
        }
    }

    public void deleteByProblemId(int problemId) {
        String sql = "DELETE FROM test_cases WHERE problem_id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, problemId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to delete problem test cases.", exception);
        }
    }

    private void insert(TestCase testCase) {
        String sql = "INSERT INTO test_cases (problem_id, input, expected, is_sample, sequence_order) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, testCase.getProblemId());
                statement.setString(2, testCase.getInput());
                statement.setString(3, testCase.getExpected());
                statement.setInt(4, testCase.isSample() ? 1 : 0);
                statement.setInt(5, testCase.getSequenceOrder());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        testCase.setId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to create test case.", exception);
        }
    }

    private void update(TestCase testCase) {
        String sql = "UPDATE test_cases SET problem_id = ?, input = ?, expected = ?, is_sample = ?, sequence_order = ? WHERE id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, testCase.getProblemId());
                statement.setString(2, testCase.getInput());
                statement.setString(3, testCase.getExpected());
                statement.setInt(4, testCase.isSample() ? 1 : 0);
                statement.setInt(5, testCase.getSequenceOrder());
                statement.setInt(6, testCase.getId());
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update test case.", exception);
        }
    }

    private List<TestCase> mapTestCases(ResultSet resultSet) throws SQLException {
        List<TestCase> testCases = new ArrayList<>();
        while (resultSet.next()) {
            TestCase testCase = new TestCase();
            testCase.setId(resultSet.getInt("id"));
            testCase.setProblemId(resultSet.getInt("problem_id"));
            testCase.setInput(decodeEscapedNewlines(resultSet.getString("input")));
            testCase.setExpected(decodeEscapedNewlines(resultSet.getString("expected")));
            testCase.setSample(resultSet.getInt("is_sample") == 1);
            testCase.setSequenceOrder(resultSet.getInt("sequence_order"));
            testCases.add(testCase);
        }
        return testCases;
    }

    private String decodeEscapedNewlines(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\r\\n", "\n").replace("\\n", "\n").replace("\\t", "\t");
    }
}
