package com.codearena.service;

import com.codearena.dao.SquadDAO;
import com.codearena.dao.UserDAO;
import com.codearena.model.Squad;
import com.codearena.model.User;
import com.codearena.util.DAOException;
import com.codearena.util.SessionManager;
import java.util.Comparator;
import java.util.List;

public class SquadService {

    private final SquadDAO squadDAO;
    private final UserDAO userDAO;

    public SquadService() {
        this(new SquadDAO(), new UserDAO());
    }

    public SquadService(SquadDAO squadDAO, UserDAO userDAO) {
        this.squadDAO = squadDAO;
        this.userDAO = userDAO;
    }

    public Squad getCurrentUserSquad(User user) {
        try {
            if (user == null || user.getSquadId() == null) {
                return null;
            }
            return squadDAO.getById(user.getSquadId());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load squad.");
        }
    }

    public Squad createSquad(User leader, String name, String description) {
        try {
            if (leader == null) {
                throw new DAOException("You must be logged in.");
            }
            if (leader.getSquadId() != null) {
                throw new DAOException("You are already in a squad.");
            }
            if (name == null || name.isBlank()) {
                throw new DAOException("Squad name is required.");
            }
            if (squadDAO.findByName(name.trim()) != null) {
                throw new DAOException("Squad name is already taken.");
            }

            Squad squad = new Squad();
            squad.setName(name.trim());
            squad.setDescription(description == null ? "" : description.trim());
            squad.setLeaderId(leader.getId());
            squadDAO.save(squad);
            userDAO.updateSquad(leader.getId(), squad.getId());
            refreshSession(leader.getId());
            return squad;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to create squad.");
        }
    }

    public void joinSquad(User user, String squadName) {
        try {
            if (user == null) {
                throw new DAOException("You must be logged in.");
            }
            if (user.getSquadId() != null) {
                throw new DAOException("You are already in a squad.");
            }
            Squad squad = squadDAO.findByName(squadName == null ? "" : squadName.trim());
            if (squad == null) {
                throw new DAOException("Squad not found.");
            }
            userDAO.updateSquad(user.getId(), squad.getId());
            refreshSession(user.getId());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to join squad.");
        }
    }

    public void leaveSquad(User user) {
        try {
            if (user == null || user.getSquadId() == null) {
                return;
            }
            Squad squad = squadDAO.getById(user.getSquadId());
            if (squad != null && squad.getLeaderId() == user.getId()) {
                throw new DAOException("Leaders must remove members or keep the squad.");
            }
            userDAO.updateSquad(user.getId(), null);
            refreshSession(user.getId());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to leave squad.");
        }
    }

    public void removeMember(User leader, int memberId) {
        try {
            Squad squad = getCurrentUserSquad(leader);
            if (squad == null || squad.getLeaderId() != leader.getId()) {
                throw new DAOException("Only the squad leader can remove members.");
            }
            if (memberId == leader.getId()) {
                throw new DAOException("Leader cannot remove themselves.");
            }
            userDAO.updateSquad(memberId, null);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to remove member.");
        }
    }

    public List<User> getMembers(Squad squad) {
        try {
            if (squad == null) {
                return List.of();
            }
            return squadDAO.getMembers(squad.getId());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load squad members.");
        }
    }

    public List<Squad> getSquadLeaderboard() {
        try {
            return squadDAO.getAll().stream()
                    .sorted(Comparator.comparingInt((Squad squad) -> getCombinedXp(squad)).reversed())
                    .toList();
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load squad leaderboard.");
        }
    }

    public int getCombinedXp(Squad squad) {
        return getMembers(squad).stream().mapToInt(User::getXp).sum();
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
