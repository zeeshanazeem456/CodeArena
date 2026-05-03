package com.codearena.dao;

import com.codearena.model.Badge;
import com.codearena.util.DAOException;
import com.codearena.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BadgeDAO {

    public List<Badge> findAllWithUserStatus(int userId) {
        String sql = """
                SELECT b.id, b.code, b.name, b.description, b.category, b.image_path,
                       b.sort_order, b.is_active, b.created_at, ub.earned_at
                FROM badges b
                LEFT JOIN user_badges ub ON ub.badge_id = b.id AND ub.user_id = ?
                WHERE b.is_active = 1
                ORDER BY b.sort_order ASC, b.name ASC
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return mapBadges(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to load badges.", exception);
        }
    }

    public void awardBadge(int userId, String badgeCode) {
        String sql = """
                INSERT OR IGNORE INTO user_badges (user_id, badge_id)
                SELECT ?, id
                FROM badges
                WHERE code = ? AND is_active = 1
                """;

        try {
            Connection connection = DBConnection.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setString(2, badgeCode);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new DAOException("Failed to award badge.", exception);
        }
    }

    private List<Badge> mapBadges(ResultSet resultSet) throws SQLException {
        List<Badge> badges = new ArrayList<>();
        while (resultSet.next()) {
            Badge badge = new Badge();
            badge.setId(resultSet.getInt("id"));
            badge.setCode(resultSet.getString("code"));
            badge.setName(resultSet.getString("name"));
            badge.setDescription(resultSet.getString("description"));
            badge.setCategory(resultSet.getString("category"));
            badge.setImagePath(resultSet.getString("image_path"));
            badge.setSortOrder(resultSet.getInt("sort_order"));
            badge.setActive(resultSet.getInt("is_active") == 1);
            badge.setCreatedAt(resultSet.getString("created_at"));
            badge.setEarnedAt(resultSet.getString("earned_at"));
            badges.add(badge);
        }
        return badges;
    }
}
