package com.codearena.dao;

import com.codearena.model.Squad;
import com.codearena.model.User;
import com.codearena.util.DAOException;
import com.codearena.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SquadDAO extends BaseDAO<Squad> {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public Squad getById(int id) {
        String sql = "SELECT id, name, description, leader_id, created_at FROM squads WHERE id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapSquad(resultSet) : null;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load squad.", exception);
        }
    }

    public Squad findByName(String name) {
        String sql = "SELECT id, name, description, leader_id, created_at FROM squads WHERE lower(name) = lower(?)";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapSquad(resultSet) : null;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to find squad.", exception);
        }
    }

    public List<Squad> getAll() {
        String sql = "SELECT id, name, description, leader_id, created_at FROM squads ORDER BY name ASC";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                List<Squad> squads = new ArrayList<>();
                while (resultSet.next()) {
                    squads.add(mapSquad(resultSet));
                }
                return squads;
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load squads.", exception);
        }
    }

    public List<User> getMembers(int squadId) {
        String sql = """
                SELECT id, username, email, password, role, xp, rank_title, problems_solved,
                       battles_won, battles_lost, streak_days, squad_id, created_at, is_active
                FROM users
                WHERE squad_id = ?
                ORDER BY xp DESC, username ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, squadId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<User> members = new ArrayList<>();
                    while (resultSet.next()) {
                        User user = userDAO.findById(resultSet.getInt("id"));
                        if (user != null) {
                            members.add(user);
                        }
                    }
                    return members;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load squad members.", exception);
        }
    }

    @Override
    public void save(Squad squad) {
        if (squad.getId() == 0) {
            insert(squad);
        } else {
            update(squad);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM squads WHERE id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to delete squad.", exception);
        }
    }

    private void insert(Squad squad) {
        String sql = "INSERT INTO squads (name, description, leader_id) VALUES (?, ?, ?)";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, squad.getName());
                statement.setString(2, squad.getDescription());
                statement.setInt(3, squad.getLeaderId());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        squad.setId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to create squad.", exception);
        }
    }

    private void update(Squad squad) {
        String sql = "UPDATE squads SET name = ?, description = ?, leader_id = ? WHERE id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, squad.getName());
                statement.setString(2, squad.getDescription());
                statement.setInt(3, squad.getLeaderId());
                statement.setInt(4, squad.getId());
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update squad.", exception);
        }
    }

    private Squad mapSquad(ResultSet resultSet) throws SQLException {
        Squad squad = new Squad();
        squad.setId(resultSet.getInt("id"));
        squad.setName(resultSet.getString("name"));
        squad.setDescription(resultSet.getString("description"));
        squad.setLeaderId(resultSet.getInt("leader_id"));
        squad.setCreatedAt(resultSet.getString("created_at"));
        return squad;
    }
}
