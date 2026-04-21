package com.codearena.dao;

import com.codearena.judge.Verdict;
import com.codearena.model.Submission;
import com.codearena.util.DAOException;
import com.codearena.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDAO extends BaseDAO<Submission> {

    @Override
    public Submission getById(int id) {
        return findById(id);
    }

    public Submission findById(int id) {
        String sql = """
                SELECT id, user_id, problem_id, code, language, verdict, runtime_ms, battle_id, submitted_at
                FROM submissions
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return mapSubmission(resultSet);
                    }
                    return null;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load submission by id.", exception);
        }
    }

    @Override
    public void save(Submission submission) {
        if (submission.getId() == 0) {
            insert(submission);
        } else {
            update(submission);
        }
    }

    public List<Submission> findByUserId(int userId) {
        String sql = """
                SELECT id, user_id, problem_id, code, language, verdict, runtime_ms, battle_id, submitted_at
                FROM submissions
                WHERE user_id = ?
                ORDER BY submitted_at DESC, id DESC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapSubmissions(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load submissions by user.", exception);
        }
    }

    public List<Submission> findByProblemId(int problemId) {
        String sql = """
                SELECT id, user_id, problem_id, code, language, verdict, runtime_ms, battle_id, submitted_at
                FROM submissions
                WHERE problem_id = ?
                ORDER BY submitted_at DESC, id DESC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, problemId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapSubmissions(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load submissions by problem.", exception);
        }
    }

    public boolean hasAccepted(int userId, int problemId) {
        String sql = """
                SELECT 1
                FROM submissions
                WHERE user_id = ? AND problem_id = ? AND verdict = 'AC'
                LIMIT 1
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setInt(2, problemId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to check accepted submission history.", exception);
        }
    }

    public boolean hasAcceptedSubmission(int problemId, int userId) {
        return hasAccepted(userId, problemId);
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM submissions WHERE id = ?";

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to delete submission.", exception);
        }
    }

    private void insert(Submission submission) {
        String sql = """
                INSERT INTO submissions (user_id, problem_id, code, language, verdict, runtime_ms, battle_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindSubmission(statement, submission, false);
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        submission.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to insert submission.", exception);
        }
    }

    private void update(Submission submission) {
        String sql = """
                UPDATE submissions
                SET user_id = ?, problem_id = ?, code = ?, language = ?, verdict = ?, runtime_ms = ?, battle_id = ?
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindSubmission(statement, submission, true);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update submission.", exception);
        }
    }

    private void bindSubmission(PreparedStatement statement, Submission submission, boolean includeId) throws SQLException {
        statement.setInt(1, submission.getUserId());
        statement.setInt(2, submission.getProblemId());
        statement.setString(3, submission.getCode());
        statement.setString(4, submission.getLanguage() == null ? "Java" : submission.getLanguage());
        Verdict verdict = submission.getVerdict();
        statement.setString(5, verdict == null ? "PENDING" : verdict.name());

        if (submission.getRuntimeMs() == null) {
            statement.setNull(6, java.sql.Types.INTEGER);
        } else {
            statement.setInt(6, submission.getRuntimeMs());
        }

        if (submission.getBattleId() == null) {
            statement.setNull(7, java.sql.Types.INTEGER);
        } else {
            statement.setInt(7, submission.getBattleId());
        }

        if (includeId) {
            statement.setInt(8, submission.getId());
        }
    }

    private List<Submission> mapSubmissions(ResultSet resultSet) throws SQLException {
        List<Submission> submissions = new ArrayList<>();
        while (resultSet.next()) {
            submissions.add(mapSubmission(resultSet));
        }
        return submissions;
    }

    private Submission mapSubmission(ResultSet resultSet) throws SQLException {
        Submission submission = new Submission();
        submission.setId(resultSet.getInt("id"));
        submission.setUserId(resultSet.getInt("user_id"));
        submission.setProblemId(resultSet.getInt("problem_id"));
        submission.setCode(resultSet.getString("code"));
        submission.setLanguage(resultSet.getString("language"));
        submission.setVerdict(mapVerdict(resultSet.getString("verdict")));

        int runtimeMs = resultSet.getInt("runtime_ms");
        submission.setRuntimeMs(resultSet.wasNull() ? null : runtimeMs);

        int battleId = resultSet.getInt("battle_id");
        submission.setBattleId(resultSet.wasNull() ? null : battleId);

        String submittedAt = resultSet.getString("submitted_at");
        submission.setSubmittedAt(submittedAt);
        submission.setCreatedAt(submittedAt);
        return submission;
    }

    private Verdict mapVerdict(String verdict) {
        if (verdict == null || verdict.isBlank() || "PENDING".equalsIgnoreCase(verdict)) {
            return null;
        }
        return Verdict.valueOf(verdict);
    }
}
