package com.codearena.service;

import com.codearena.dao.BattleDAO;
import com.codearena.dao.ProblemDAO;
import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.UserDAO;
import com.codearena.model.Battle;
import com.codearena.model.Badge;
import com.codearena.model.Submission;
import com.codearena.model.User;
import com.codearena.util.DAOException;
import com.codearena.util.SessionManager;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class ProfileService {

    private final UserDAO userDAO;
    private final SubmissionDAO submissionDAO;
    private final BattleDAO battleDAO;
    private final ProblemDAO problemDAO;
    private final BadgeService badgeService;

    public ProfileService() {
        this(new UserDAO(), new SubmissionDAO(), new BattleDAO(), new ProblemDAO(), new BadgeService());
    }

    public ProfileService(UserDAO userDAO, SubmissionDAO submissionDAO, BattleDAO battleDAO, ProblemDAO problemDAO) {
        this(userDAO, submissionDAO, battleDAO, problemDAO, new BadgeService());
    }

    public ProfileService(UserDAO userDAO, SubmissionDAO submissionDAO, BattleDAO battleDAO,
                          ProblemDAO problemDAO, BadgeService badgeService) {
        this.userDAO = userDAO;
        this.submissionDAO = submissionDAO;
        this.battleDAO = battleDAO;
        this.problemDAO = problemDAO;
        this.badgeService = badgeService;
    }

    public User getUser(int userId) {
        try {
            return userDAO.findById(userId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load profile.");
        }
    }

    public List<Submission> getSubmissionHistory(int userId) {
        try {
            return submissionDAO.findByUserId(userId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load submission history.");
        }
    }

    public List<Battle> getBattleHistory(int userId) {
        try {
            List<Battle> battles = battleDAO.findByUserId(userId);
            boolean expiredAny = false;
            BattleService battleService = new BattleService();
            for (Battle battle : battles) {
                if ("ACTIVE".equalsIgnoreCase(battle.getStatus()) && battleService.expireIfNeeded(battle)) {
                    expiredAny = true;
                }
            }
            return expiredAny ? battleDAO.findByUserId(userId) : battles;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load battle history.");
        }
    }

    public List<Badge> getBadges(int userId) {
        try {
            User user = userDAO.findById(userId);
            badgeService.syncUserBadges(user);
            return badgeService.getBadgesForUser(userId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load badges.");
        }
    }

    public void updateProfile(User user, String username, String email, String newPassword) {
        try {
            if (username == null || username.isBlank() || email == null || email.isBlank()) {
                throw new DAOException("Username and email are required.");
            }
            String passwordHash = null;
            if (newPassword != null && !newPassword.isBlank()) {
                if (newPassword.length() < 8) {
                    throw new DAOException("Password must be at least 8 characters.");
                }
                passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            }

            userDAO.updateProfile(user.getId(), username.trim(), email.trim(), passwordHash);
            User refreshed = userDAO.findById(user.getId());
            if (refreshed != null) {
                SessionManager.setCurrentUser(refreshed);
            }
        } catch (Exception exception) {
            throw wrap(exception, "Failed to update profile.");
        }
    }

    public String getProblemTitle(int problemId) {
        try {
            var problem = problemDAO.getById(problemId);
            return problem == null ? "#" + problemId : problem.getTitle();
        } catch (Exception exception) {
            return "#" + problemId;
        }
    }

    public String getUsername(int userId) {
        try {
            User user = userDAO.findById(userId);
            return user == null ? "#" + userId : user.getUsername();
        } catch (Exception exception) {
            return "#" + userId;
        }
    }

    public String getBattleOpponent(Battle battle, int currentUserId) {
        if (battle == null) {
            return "-";
        }
        String mode = battle.getBattleMode() == null ? "" : battle.getBattleMode();
        if ("FREE_FOR_ALL".equalsIgnoreCase(mode)) {
            return "Free for all";
        }
        if ("PENDING".equalsIgnoreCase(battle.getStatus())) {
            return "Waiting for player";
        }
        int opponentId = battle.getPlayer1Id() == currentUserId ? battle.getPlayer2Id() : battle.getPlayer1Id();
        return getUsername(opponentId);
    }

    public String getBattleOutcome(Battle battle, int currentUserId) {
        if (battle == null) {
            return "-";
        }
        if (battle.getWinnerId() != null) {
            return battle.getWinnerId() == currentUserId ? "Win" : "Loss";
        }
        String status = battle.getStatus() == null ? "" : battle.getStatus().trim().toUpperCase();
        return switch (status) {
            case "DRAW" -> "Draw";
            case "FINISHED" -> "Finished";
            case "ACTIVE" -> "In Progress";
            case "MATCHED" -> "Ready";
            case "PENDING" -> "Waiting";
            default -> status.isBlank() ? "-" : status;
        };
    }

    private DAOException wrap(Exception exception, String message) {
        if (exception instanceof DAOException daoException) {
            return daoException;
        }
        return new DAOException(message, exception);
    }
}
