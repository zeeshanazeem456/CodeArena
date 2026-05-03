package com.codearena.dao;

import com.codearena.model.Battle;
import com.codearena.util.DAOException;
import com.codearena.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BattleDAO extends BaseDAO<Battle> {

    @Override
    public Battle getById(int id) {
        String sql = """
                SELECT id, player1_id, player2_id, problem_id, winner_id, join_code, battle_mode, status, started_at, finished_at, time_limit
                FROM battles
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapBattle(resultSet) : null;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load battle.", exception);
        }
    }

    public List<Battle> findByUserId(int userId) {
        String sql = """
                SELECT id, player1_id, player2_id, problem_id, winner_id, join_code, battle_mode, status, started_at, finished_at, time_limit
                FROM battles
                WHERE player1_id = ? OR player2_id = ?
                   OR id IN (SELECT battle_id FROM battle_participants WHERE user_id = ?)
                ORDER BY COALESCE(finished_at, started_at) DESC, id DESC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setInt(2, userId);
                statement.setInt(3, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Battle> battles = new ArrayList<>();
                    while (resultSet.next()) {
                        battles.add(mapBattle(resultSet));
                    }
                    return battles;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load battle history.", exception);
        }
    }

    public List<Battle> findActiveByUserId(int userId) {
        String sql = """
                SELECT id, player1_id, player2_id, problem_id, winner_id, join_code, battle_mode, status, started_at, finished_at, time_limit
                FROM battles
                WHERE (player1_id = ? OR player2_id = ?
                   OR id IN (SELECT battle_id FROM battle_participants WHERE user_id = ?))
                  AND UPPER(status) = 'ACTIVE'
                ORDER BY started_at DESC, id DESC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setInt(2, userId);
                statement.setInt(3, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Battle> battles = new ArrayList<>();
                    while (resultSet.next()) {
                        battles.add(mapBattle(resultSet));
                    }
                    return battles;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load active battles.", exception);
        }
    }

    public List<Battle> findPendingByCreatorId(int userId) {
        String sql = """
                SELECT id, player1_id, player2_id, problem_id, winner_id, join_code, battle_mode, status, started_at, finished_at, time_limit
                FROM battles
                WHERE player1_id = ?
                  AND UPPER(status) = 'PENDING'
                ORDER BY id DESC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Battle> battles = new ArrayList<>();
                    while (resultSet.next()) {
                        battles.add(mapBattle(resultSet));
                    }
                    return battles;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load pending battle rooms.", exception);
        }
    }

    public Battle findByJoinCode(String joinCode) {
        String sql = """
                SELECT id, player1_id, player2_id, problem_id, winner_id, join_code, battle_mode, status, started_at, finished_at, time_limit
                FROM battles
                WHERE UPPER(join_code) = UPPER(?)
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, joinCode);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapBattle(resultSet) : null;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load battle room.", exception);
        }
    }

    public Battle findQueuedRandomMatch(int userId, String difficulty) {
        String sql = """
                SELECT b.id, b.player1_id, b.player2_id, b.problem_id, b.winner_id, b.join_code,
                       b.battle_mode, b.status, b.started_at, b.finished_at, b.time_limit
                FROM battles b
                JOIN problems p ON p.id = b.problem_id
                WHERE UPPER(b.battle_mode) = 'RANDOM_ONE_V_ONE'
                  AND UPPER(b.status) = 'PENDING'
                  AND b.player1_id <> ?
                  AND p.difficulty = ?
                ORDER BY b.id ASC
                LIMIT 1
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setString(2, difficulty);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapBattle(resultSet) : null;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to find random match.", exception);
        }
    }

    public void addParticipant(int battleId, int userId) {
        String sql = """
                INSERT OR IGNORE INTO battle_participants (battle_id, user_id)
                VALUES (?, ?)
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, battleId);
                statement.setInt(2, userId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to add battle participant.", exception);
        }
    }

    public boolean hasParticipant(int battleId, int userId) {
        String sql = "SELECT 1 FROM battle_participants WHERE battle_id = ? AND user_id = ?";

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, battleId);
                statement.setInt(2, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to check battle participant.", exception);
        }
    }

    public int countParticipants(int battleId) {
        String sql = "SELECT COUNT(*) FROM battle_participants WHERE battle_id = ?";

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, battleId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to count battle participants.", exception);
        }
    }

    public void markParticipantReady(int battleId, int userId) {
        String sql = """
                UPDATE battle_participants
                SET ready_at = COALESCE(ready_at, datetime('now'))
                WHERE battle_id = ? AND user_id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, battleId);
                statement.setInt(2, userId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to mark battle participant ready.", exception);
        }
    }

    public int countReadyParticipants(int battleId) {
        String sql = "SELECT COUNT(*) FROM battle_participants WHERE battle_id = ? AND ready_at IS NOT NULL";

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, battleId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to count ready battle participants.", exception);
        }
    }

    public int countCompletedByUserId(int userId) {
        String sql = """
                SELECT COUNT(*)
                FROM battles
                WHERE (player1_id = ? OR player2_id = ?
                   OR id IN (SELECT battle_id FROM battle_participants WHERE user_id = ?))
                  AND UPPER(status) IN ('FINISHED', 'DRAW')
                """;
        return countByUser(sql, userId);
    }

    public int countCompletedRandomByUserId(int userId) {
        String sql = """
                SELECT COUNT(*)
                FROM battles
                WHERE (player1_id = ? OR player2_id = ?
                   OR id IN (SELECT battle_id FROM battle_participants WHERE user_id = ?))
                  AND UPPER(status) IN ('FINISHED', 'DRAW')
                  AND UPPER(battle_mode) = 'RANDOM_ONE_V_ONE'
                """;
        return countByUser(sql, userId);
    }

    private int countByUser(String sql, int userId) {
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setInt(2, userId);
                statement.setInt(3, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? resultSet.getInt(1) : 0;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to count battles.", exception);
        }
    }

    @Override
    public void save(Battle battle) {
        if (battle.getId() == 0) {
            insert(battle);
        } else {
            update(battle);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM battles WHERE id = ?";
        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to delete battle.", exception);
        }
    }

    private void insert(Battle battle) {
        String sql = """
                INSERT INTO battles (player1_id, player2_id, problem_id, winner_id, join_code, battle_mode, status, started_at, finished_at, time_limit)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindBattle(statement, battle, false);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        battle.setId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to create battle.", exception);
        }
    }

    private void update(Battle battle) {
        String sql = """
                UPDATE battles
                SET player1_id = ?, player2_id = ?, problem_id = ?, winner_id = ?, join_code = ?,
                    battle_mode = ?, status = ?, started_at = ?, finished_at = ?, time_limit = ?
                WHERE id = ?
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindBattle(statement, battle, true);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to update battle.", exception);
        }
    }

    private void bindBattle(PreparedStatement statement, Battle battle, boolean includeId) throws SQLException {
        statement.setInt(1, battle.getPlayer1Id());
        statement.setInt(2, battle.getPlayer2Id());
        statement.setInt(3, battle.getProblemId());
        if (battle.getWinnerId() == null) {
            statement.setNull(4, java.sql.Types.INTEGER);
        } else {
            statement.setInt(4, battle.getWinnerId());
        }
        statement.setString(5, battle.getJoinCode());
        statement.setString(6, battle.getBattleMode() == null ? "ONE_V_ONE" : battle.getBattleMode());
        statement.setString(7, battle.getStatus() == null ? "ACTIVE" : battle.getStatus());
        statement.setString(8, battle.getStartedAt());
        statement.setString(9, battle.getFinishedAt());
        statement.setInt(10, battle.getTimeLimit() == 0 ? 1800 : battle.getTimeLimit());
        if (includeId) {
            statement.setInt(11, battle.getId());
        }
    }

    private Battle mapBattle(ResultSet resultSet) throws SQLException {
        Battle battle = new Battle();
        battle.setId(resultSet.getInt("id"));
        battle.setPlayer1Id(resultSet.getInt("player1_id"));
        battle.setPlayer2Id(resultSet.getInt("player2_id"));
        battle.setProblemId(resultSet.getInt("problem_id"));
        int winnerId = resultSet.getInt("winner_id");
        battle.setWinnerId(resultSet.wasNull() ? null : winnerId);
        battle.setJoinCode(resultSet.getString("join_code"));
        battle.setBattleMode(resultSet.getString("battle_mode"));
        battle.setStatus(resultSet.getString("status"));
        battle.setStartedAt(resultSet.getString("started_at"));
        battle.setFinishedAt(resultSet.getString("finished_at"));
        battle.setTimeLimit(resultSet.getInt("time_limit"));
        battle.setCreatedAt(resultSet.getString("started_at"));
        return battle;
    }
}
