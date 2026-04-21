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

public class TestCaseDAO {

    public List<TestCase> getByProblemId(int problemId) {
        String sql = """
                SELECT id, problem_id, input, expected, is_sample
                FROM test_cases
                WHERE problem_id = ?
                ORDER BY id ASC
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
                SELECT id, problem_id, input, expected, is_sample
                FROM test_cases
                WHERE problem_id = ? AND is_sample = 1
                ORDER BY id ASC
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

    private List<TestCase> mapTestCases(ResultSet resultSet) throws SQLException {
        List<TestCase> testCases = new ArrayList<>();
        while (resultSet.next()) {
            TestCase testCase = new TestCase();
            testCase.setId(resultSet.getInt("id"));
            testCase.setProblemId(resultSet.getInt("problem_id"));
            testCase.setInput(resultSet.getString("input"));
            testCase.setExpected(resultSet.getString("expected"));
            testCase.setSample(resultSet.getInt("is_sample") == 1);
            testCases.add(testCase);
        }
        return testCases;
    }
}
