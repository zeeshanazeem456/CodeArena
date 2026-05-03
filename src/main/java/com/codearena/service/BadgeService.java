package com.codearena.service;

import com.codearena.dao.BadgeDAO;
import com.codearena.dao.BattleDAO;
import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.UserDAO;
import com.codearena.model.Badge;
import com.codearena.model.Problem;
import com.codearena.model.Submission;
import com.codearena.model.User;
import com.codearena.util.DAOException;
import java.util.List;

public class BadgeService {

    private final BadgeDAO badgeDAO;
    private final SubmissionDAO submissionDAO;
    private final BattleDAO battleDAO;
    private final UserDAO userDAO;

    public BadgeService() {
        this(new BadgeDAO(), new SubmissionDAO(), new BattleDAO(), new UserDAO());
    }

    public BadgeService(BadgeDAO badgeDAO, SubmissionDAO submissionDAO, BattleDAO battleDAO, UserDAO userDAO) {
        this.badgeDAO = badgeDAO;
        this.submissionDAO = submissionDAO;
        this.battleDAO = battleDAO;
        this.userDAO = userDAO;
    }

    public List<Badge> getBadgesForUser(int userId) {
        try {
            return badgeDAO.findAllWithUserStatus(userId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load badges.");
        }
    }

    public void syncUserBadges(User user) {
        try {
            if (user == null) {
                return;
            }
            int userId = user.getId();
            int accepted = submissionDAO.countAcceptedProblems(userId);
            if (accepted >= 1) {
                award(userId, "FIRST_SOLVE");
            }
            if (accepted >= 5) {
                award(userId, "SOLVE_5");
            }
            if (accepted >= 25) {
                award(userId, "SOLVE_25");
            }
            if (submissionDAO.countAcceptedByDifficulty(userId, "Hard") >= 1) {
                award(userId, "FIRST_HARD");
            }
            if (submissionDAO.countAcceptedByLanguage(userId, "Java") >= 1) {
                award(userId, "JAVA_SOLVER");
            }
            if (submissionDAO.countAcceptedByLanguage(userId, "Python") >= 1) {
                award(userId, "PYTHON_SOLVER");
            }
            checkStreakBadges(userId);
            checkBattleBadges(user);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to sync badges.");
        }
    }

    public void checkSubmissionBadges(User user, Submission submission, Problem problem) {
        try {
            if (user == null || submission == null || problem == null) {
                return;
            }
            int userId = user.getId();
            int accepted = submissionDAO.countAcceptedProblems(userId);
            if (accepted >= 1) {
                award(userId, "FIRST_SOLVE");
            }
            if (accepted >= 5) {
                award(userId, "SOLVE_5");
            }
            if (accepted >= 25) {
                award(userId, "SOLVE_25");
            }
            if (submissionDAO.countAcceptedByDifficulty(userId, "Hard") >= 1) {
                award(userId, "FIRST_HARD");
            }
            if (submissionDAO.countAcceptedByLanguage(userId, "Java") >= 1) {
                award(userId, "JAVA_SOLVER");
            }
            if (submissionDAO.countAcceptedByLanguage(userId, "Python") >= 1) {
                award(userId, "PYTHON_SOLVER");
            }
            checkStreakBadges(userId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to update submission badges.");
        }
    }

    public void checkBattleBadges(User user) {
        try {
            if (user == null) {
                return;
            }
            int userId = user.getId();
            User refreshed = userDAO.findById(userId);
            int completedBattles = battleDAO.countCompletedByUserId(userId);
            if (completedBattles >= 1) {
                award(userId, "FIRST_BATTLE");
            }
            if (refreshed != null && refreshed.getBattlesWon() >= 1) {
                award(userId, "FIRST_BATTLE_WIN");
            }
            if (refreshed != null && refreshed.getBattlesWon() >= 5) {
                award(userId, "BATTLE_WINS_5");
            }
            if (battleDAO.countCompletedRandomByUserId(userId) >= 10) {
                award(userId, "RANDOM_BATTLES_10");
            }
        } catch (Exception exception) {
            throw wrap(exception, "Failed to update battle badges.");
        }
    }

    private void checkStreakBadges(int userId) {
        User refreshed = userDAO.findById(userId);
        if (refreshed == null) {
            return;
        }
        if (refreshed.getStreakDays() >= 3) {
            award(userId, "STREAK_3");
        }
        if (refreshed.getStreakDays() >= 7) {
            award(userId, "STREAK_7");
        }
    }

    private void award(int userId, String badgeCode) {
        badgeDAO.awardBadge(userId, badgeCode);
    }

    private DAOException wrap(Exception exception, String message) {
        if (exception instanceof DAOException daoException) {
            return daoException;
        }
        return new DAOException(message, exception);
    }
}
