package com.codearena.service;

import com.codearena.dao.UserDAO;
import com.codearena.model.User;
import com.codearena.util.DAOException;
import java.util.List;

public class LeaderboardService {

    private final UserDAO userDAO;

    public LeaderboardService() {
        this(new UserDAO());
    }

    public LeaderboardService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<User> getRankedUsers() {
        try {
            return userDAO.getAllRanked();
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load leaderboard.");
        }
    }

    private DAOException wrap(Exception exception, String message) {
        if (exception instanceof DAOException daoException) {
            return daoException;
        }
        return new DAOException(message, exception);
    }
}
