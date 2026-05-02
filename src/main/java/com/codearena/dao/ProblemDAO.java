package com.codearena.dao;

import com.codearena.model.Difficulty;
import com.codearena.model.Problem;
import com.codearena.util.DAOException;
import com.codearena.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProblemDAO extends BaseDAO<Problem> {

    public List<Problem> getAll() {
        String sql = """
                SELECT id, title, description, constraints, input_format, output_format, difficulty, category, tags, time_limit, memory_limit,
                       is_published, created_at
                FROM problems
                WHERE is_published = 1
                ORDER BY id ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                return mapProblems(resultSet);
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load published problems.", exception);
        }
    }

    public List<Problem> getAllIncludingDrafts() {
        String sql = """
                SELECT id, title, description, constraints, input_format, output_format, difficulty, category, tags, time_limit, memory_limit,
                       is_published, created_at
                FROM problems
                ORDER BY id ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                return mapProblems(resultSet);
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load all problems.", exception);
        }
    }

    @Override
    public Problem getById(int id) {
        String sql = """
                SELECT id, title, description, constraints, input_format, output_format, difficulty, category, tags, time_limit, memory_limit,
                       is_published, created_at
                FROM problems
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return mapProblem(resultSet);
                    }
                    return null;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load problem by id.", exception);
        }
    }

    public List<Problem> filterByDifficulty(String difficulty) {
        if (difficulty == null || difficulty.trim().isEmpty()) {
            return getAll();
        }

        String sql = """
                SELECT id, title, description, constraints, input_format, output_format, difficulty, category, tags, time_limit, memory_limit,
                       is_published, created_at
                FROM problems
                WHERE is_published = 1 AND difficulty = ?
                ORDER BY id ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizeDifficulty(difficulty));

                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapProblems(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to filter problems by difficulty.", exception);
        }
    }

    public List<Problem> filterByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }

        String sql = """
                SELECT id, title, description, constraints, input_format, output_format, difficulty, category, tags, time_limit, memory_limit,
                       is_published, created_at
                FROM problems
                WHERE is_published = 1 AND title LIKE ?
                ORDER BY title ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, "%" + keyword.trim() + "%");

                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapProblems(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to filter problems by title.", exception);
        }
    }

    public List<Problem> filterByCategoryOrTag(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }

        String sql = """
                SELECT id, title, description, constraints, input_format, output_format, difficulty, category, tags, time_limit, memory_limit,
                       is_published, created_at
                FROM problems
                WHERE is_published = 1 AND (category LIKE ? OR tags LIKE ?)
                ORDER BY title ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = "%" + keyword.trim() + "%";
                statement.setString(1, pattern);
                statement.setString(2, pattern);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapProblems(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to filter problems by category or tag.", exception);
        }
    }

    public double getAcceptanceRate(int problemId) {
        String sql = """
                SELECT
                    COUNT(*) AS total_count,
                    SUM(CASE WHEN verdict = 'AC' THEN 1 ELSE 0 END) AS accepted_count
                FROM submissions
                WHERE problem_id = ? AND verdict <> 'PENDING'
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, problemId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return 0.0;
                    }
                    int total = resultSet.getInt("total_count");
                    int accepted = resultSet.getInt("accepted_count");
                    return total == 0 ? 0.0 : (accepted * 100.0) / total;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to calculate acceptance rate.", exception);
        }
    }

    public Problem getRandomPublishedByDifficulty(String difficulty) {
        String sql = """
                SELECT id, title, description, constraints, input_format, output_format, difficulty, category, tags, time_limit, memory_limit,
                       is_published, created_at
                FROM problems
                WHERE is_published = 1 AND difficulty = ?
                ORDER BY random()
                LIMIT 1
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizeDifficulty(difficulty));
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapProblem(resultSet) : null;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to select battle problem.", exception);
        }
    }

    @Override
    public void save(Problem problem) {
        if (problem.getId() == 0) {
            insert(problem);
        } else {
            update(problem);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM problems WHERE id = ?";

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to delete problem.", exception);
        }
    }

    private void insert(Problem problem) {
        String sql = """
                INSERT INTO problems (title, description, constraints, input_format, output_format, difficulty, category, tags, time_limit, memory_limit, is_published)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindProblem(statement, problem, false);
                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        problem.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to insert problem.", exception);
        }
    }

    private void update(Problem problem) {
        String sql = """
                UPDATE problems
                SET title = ?, description = ?, constraints = ?, input_format = ?, output_format = ?,
                    difficulty = ?, category = ?, tags = ?, time_limit = ?, memory_limit = ?, is_published = ?
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindProblem(statement, problem, true);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update problem.", exception);
        }
    }

    private void bindProblem(PreparedStatement statement, Problem problem, boolean includeId) throws SQLException {
        statement.setString(1, problem.getTitle());
        statement.setString(2, problem.getDescription());
        statement.setString(3, problem.getConstraints());
        statement.setString(4, problem.getInputFormat());
        statement.setString(5, problem.getOutputFormat());
        statement.setString(6, problem.getDifficulty() == null ? null : problem.getDifficulty().getLabel());
        statement.setString(7, problem.getCategory());
        statement.setString(8, problem.getTags());
        statement.setInt(9, problem.getTimeLimit());
        statement.setInt(10, problem.getMemoryLimit());
        statement.setInt(11, problem.isPublished() ? 1 : 0);

        if (includeId) {
            statement.setInt(12, problem.getId());
        }
    }

    private List<Problem> mapProblems(ResultSet resultSet) throws SQLException {
        List<Problem> problems = new ArrayList<>();
        while (resultSet.next()) {
            problems.add(mapProblem(resultSet));
        }
        return problems;
    }

    private Problem mapProblem(ResultSet resultSet) throws SQLException {
        Problem problem = new Problem();
        problem.setId(resultSet.getInt("id"));
        problem.setCreatedAt(resultSet.getString("created_at"));
        problem.setTitle(resultSet.getString("title"));
        problem.setDescription(resultSet.getString("description"));
        problem.setConstraints(resultSet.getString("constraints"));
        problem.setInputFormat(resultSet.getString("input_format"));
        problem.setOutputFormat(resultSet.getString("output_format"));
        problem.setDifficulty(mapDifficulty(resultSet.getString("difficulty")));
        problem.setCategory(resultSet.getString("category"));
        problem.setTags(resultSet.getString("tags"));
        problem.setTimeLimit(resultSet.getInt("time_limit"));
        problem.setMemoryLimit(resultSet.getInt("memory_limit"));
        problem.setPublished(resultSet.getInt("is_published") == 1);
        return problem;
    }

    private Difficulty mapDifficulty(String value) {
        if (value == null) {
            return null;
        }

        return switch (value.trim().toUpperCase()) {
            case "EASY" -> Difficulty.EASY;
            case "MEDIUM" -> Difficulty.MEDIUM;
            case "HARD" -> Difficulty.HARD;
            default -> switch (value.trim()) {
                case "Easy" -> Difficulty.EASY;
                case "Medium" -> Difficulty.MEDIUM;
                case "Hard" -> Difficulty.HARD;
                default -> throw new DAOException("Unknown difficulty value: " + value);
            };
        };
    }

    private String normalizeDifficulty(String difficulty) {
        return switch (difficulty.trim().toUpperCase()) {
            case "EASY" -> Difficulty.EASY.getLabel();
            case "MEDIUM" -> Difficulty.MEDIUM.getLabel();
            case "HARD" -> Difficulty.HARD.getLabel();
            default -> difficulty.trim();
        };
    }
}
