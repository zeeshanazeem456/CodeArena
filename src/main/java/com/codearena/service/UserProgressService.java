package com.codearena.service;

import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.UserDAO;
import com.codearena.judge.Verdict;
import com.codearena.model.Difficulty;
import com.codearena.model.User;
import com.codearena.util.DAOException;
import com.codearena.util.SessionManager;
import com.codearena.util.XPCalculator;

public class UserProgressService {

    private final UserDAO userDAO;
    private final SubmissionDAO submissionDAO;

    public UserProgressService() {
        this(new UserDAO(), new SubmissionDAO());
    }

    public UserProgressService(UserDAO userDAO, SubmissionDAO submissionDAO) {
        this.userDAO = userDAO;
        this.submissionDAO = submissionDAO;
    }

    public void awardProblemSolved(User user, int problemId, Difficulty difficulty) {
        awardProblemSolved(user, problemId, difficulty, false);
    }

    public void awardProblemSolved(User user, int problemId, Difficulty difficulty, boolean alreadySolved) {
        try {
            if (user == null || difficulty == null || alreadySolved) {
                return;
            }
            int xp = user.getXp() + XPCalculator.forSolvingProblem(difficulty.name());
            int solved = user.getProblemsSolved() + 1;
            int streak = Math.max(1, user.getStreakDays() + 1);
            String rank = XPCalculator.getRankTitle(xp);
            userDAO.updateProgress(user.getId(), xp, rank, solved, streak);
            refreshSession(user.getId());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to award XP.");
        }
    }

    public void awardBattleWin(User winner, User loser) {
        try {
            int winnerXp = winner.getXp() + XPCalculator.forWinningBattle();
            userDAO.updateBattleStats(
                    winner.getId(),
                    winnerXp,
                    XPCalculator.getRankTitle(winnerXp),
                    winner.getBattlesWon() + 1,
                    winner.getBattlesLost()
            );
            if (loser != null) {
                userDAO.updateBattleStats(
                        loser.getId(),
                        loser.getXp(),
                        loser.getRankTitle(),
                        loser.getBattlesWon(),
                        loser.getBattlesLost() + 1
                );
            }
            refreshSession(winner.getId());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to update battle progress.");
        }
    }

    private void refreshSession(int userId) {
        User refreshed = userDAO.findById(userId);
        if (refreshed != null && SessionManager.getCurrentUser() != null
                && SessionManager.getCurrentUser().getId() == userId) {
            SessionManager.setCurrentUser(refreshed);
        }
    }

    private DAOException wrap(Exception exception, String message) {
        if (exception instanceof DAOException daoException) {
            return daoException;
        }
        return new DAOException(message, exception);
    }
}
