package com.codearena.dao;

import com.codearena.model.User;
import com.codearena.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.codearena.util.DAOException;

public class UserDAO {

    public User findByUsername(String username) {
        String sql = """
                SELECT id, username, email, password, role, xp, rank_title, problems_solved,
                       battles_won, battles_lost, streak_days, squad_id, created_at, is_active
                FROM users
                WHERE username = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
                return null;
            }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find user by username.", exception);
        }
    }

    public boolean register(User user) {
        String sql = """
                INSERT INTO users (username, email, password, role)
                VALUES (?, ?, ?, ?)
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole() == null ? "CODER" : user.getRole());
            return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            return false;
        }
    }

    public void updateXP(int userId, int xp) {
        String sql = """
                UPDATE users
                SET xp = ?
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, xp);
            statement.setInt(2, userId);
            statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update user XP.", exception);
        }
    }

    public User findById(int id) {
        String sql = """
                SELECT id, username, email, password, role, xp, rank_title, problems_solved,
                       battles_won, battles_lost, streak_days, squad_id, created_at, is_active
                FROM users
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapUser(resultSet) : null;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to find user by id.", exception);
        }
    }

    public List<User> getAll() {
        String sql = """
                SELECT id, username, email, password, role, xp, rank_title, problems_solved,
                       battles_won, battles_lost, streak_days, squad_id, created_at, is_active
                FROM users
                ORDER BY username ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
                return users;
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load users.", exception);
        }
    }

    public void updateProgress(int userId, int xp, String rankTitle, int problemsSolved, int streakDays) {
        String sql = """
                UPDATE users
                SET xp = ?, rank_title = ?, problems_solved = ?, streak_days = ?, last_active = date('now')
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, xp);
                statement.setString(2, rankTitle);
                statement.setInt(3, problemsSolved);
                statement.setInt(4, streakDays);
                statement.setInt(5, userId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update user progress.", exception);
        }
    }

    public void updateBattleStats(int userId, int xp, String rankTitle, int battlesWon, int battlesLost) {
        String sql = """
                UPDATE users
                SET xp = ?, rank_title = ?, battles_won = ?, battles_lost = ?, last_active = date('now')
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, xp);
                statement.setString(2, rankTitle);
                statement.setInt(3, battlesWon);
                statement.setInt(4, battlesLost);
                statement.setInt(5, userId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update battle stats.", exception);
        }
    }

    public void updateProfile(int userId, String username, String email, String passwordHash) {
        String sql = """
                UPDATE users
                SET username = ?, email = ?, password = COALESCE(?, password)
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, email);
                statement.setString(3, passwordHash);
                statement.setInt(4, userId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update profile.", exception);
        }
    }

    public void setActive(int userId, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, active ? 1 : 0);
                statement.setInt(2, userId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update account status.", exception);
        }
    }

    public void updateSquad(int userId, Integer squadId) {
        String sql = "UPDATE users SET squad_id = ? WHERE id = ?";

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (squadId == null) {
                    statement.setNull(1, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(1, squadId);
                }
                statement.setInt(2, userId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update user squad.", exception);
        }
    }

    public List<User> getAllRanked() {
        String sql = """
                SELECT id, username, email, password, role, xp, rank_title, problems_solved,
                       battles_won, battles_lost, streak_days, squad_id, created_at, is_active
                FROM users
                ORDER BY xp DESC, problems_solved DESC, battles_won DESC, username ASC
                """;

        List<User> users = new ArrayList<>();

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
                return users;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load ranked users.", exception);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(resultSet.getString("role"));
        user.setXp(resultSet.getInt("xp"));
        user.setRankTitle(resultSet.getString("rank_title"));
        user.setProblemsSolved(resultSet.getInt("problems_solved"));
        user.setBattlesWon(resultSet.getInt("battles_won"));
        user.setBattlesLost(resultSet.getInt("battles_lost"));
        user.setStreakDays(resultSet.getInt("streak_days"));

        int squadId = resultSet.getInt("squad_id");
        if (resultSet.wasNull()) {
            user.setSquadId(null);
        } else {
            user.setSquadId(squadId);
        }

        user.setCreatedAt(resultSet.getString("created_at"));
        user.setActive(resultSet.getInt("is_active") == 1);
        return user;
    }
}
