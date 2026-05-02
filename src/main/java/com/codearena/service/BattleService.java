package com.codearena.service;

import com.codearena.dao.BattleDAO;
import com.codearena.dao.ProblemDAO;
import com.codearena.dao.UserDAO;
import com.codearena.model.Battle;
import com.codearena.model.Problem;
import com.codearena.model.User;
import com.codearena.util.DAOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

public class BattleService {

    public static final String MODE_ONE_V_ONE = "ONE_V_ONE";
    public static final String MODE_FREE_FOR_ALL = "FREE_FOR_ALL";
    public static final String MODE_RANDOM_ONE_V_ONE = "RANDOM_ONE_V_ONE";
    private static final String STATUS_MATCHED = "MATCHED";
    private static final int DEFAULT_TIME_LIMIT_SECONDS = 1800;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter SQLITE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BattleDAO battleDAO;
    private final UserDAO userDAO;
    private final ProblemDAO problemDAO;
    private final UserProgressService progressService;

    public BattleService() {
        this(new BattleDAO(), new UserDAO(), new ProblemDAO(), new UserProgressService());
    }

    public BattleService(BattleDAO battleDAO, UserDAO userDAO, ProblemDAO problemDAO, UserProgressService progressService) {
        this.battleDAO = battleDAO;
        this.userDAO = userDAO;
        this.problemDAO = problemDAO;
        this.progressService = progressService;
    }

    public List<User> getAvailableOpponents(int currentUserId) {
        try {
            return userDAO.getAll().stream()
                    .filter(User::isActive)
                    .filter(user -> user.getId() != currentUserId)
                    .filter(user -> !"ADMIN".equalsIgnoreCase(user.getRole()))
                    .toList();
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load opponents.");
        }
    }

    public List<Battle> getActiveBattles(int userId) {
        try {
            return battleDAO.findActiveByUserId(userId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load active battles.");
        }
    }

    public List<Battle> getActiveBattles(int userId, String mode) {
        return getActiveBattles(userId).stream()
                .filter(battle -> mode.equalsIgnoreCase(modeOf(battle)))
                .toList();
    }

    public List<Battle> getPendingRooms(int creatorId) {
        try {
            return battleDAO.findPendingByCreatorId(creatorId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load battle rooms.");
        }
    }

    public List<Battle> getPendingRooms(int creatorId, String mode) {
        return getPendingRooms(creatorId).stream()
                .filter(battle -> mode.equalsIgnoreCase(modeOf(battle)))
                .toList();
    }

    public Battle startBattle(int player1Id, int player2Id, String difficulty) {
        try {
            Problem problem = problemDAO.getRandomPublishedByDifficulty(difficulty);
            if (problem == null) {
                throw new DAOException("No published problem found for " + difficulty + ".");
            }

            Battle battle = new Battle();
            battle.setPlayer1Id(player1Id);
            battle.setPlayer2Id(player2Id);
            battle.setProblemId(problem.getId());
            battle.setBattleMode(MODE_ONE_V_ONE);
            battle.setStatus("ACTIVE");
            battle.setStartedAt(LocalDateTime.now().toString());
            battle.setTimeLimit(timeLimitForDifficulty(difficulty));
            battleDAO.save(battle);
            return battle;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to start battle.");
        }
    }

    public Battle createBattleRoom(int creatorId, String difficulty) {
        try {
            Problem problem = problemDAO.getRandomPublishedByDifficulty(difficulty);
            if (problem == null) {
                throw new DAOException("No published problem found for " + difficulty + ".");
            }

            Battle battle = new Battle();
            battle.setPlayer1Id(creatorId);
            battle.setPlayer2Id(creatorId);
            battle.setProblemId(problem.getId());
            battle.setJoinCode(generateUniqueJoinCode());
            battle.setBattleMode(MODE_ONE_V_ONE);
            battle.setStatus("PENDING");
            battle.setTimeLimit(timeLimitForDifficulty(difficulty));
            battleDAO.save(battle);
            battleDAO.addParticipant(battle.getId(), creatorId);
            return battle;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to create battle room.");
        }
    }

    public Battle joinBattleRoom(String joinCode, int player2Id) {
        try {
            String normalizedCode = normalizeJoinCode(joinCode);
            if (normalizedCode.isBlank()) {
                throw new DAOException("Enter a battle code.");
            }

            Battle battle = battleDAO.findByJoinCode(normalizedCode);
            if (battle == null) {
                throw new DAOException("No battle room found for that code.");
            }
            if (!"PENDING".equalsIgnoreCase(battle.getStatus())) {
                throw new DAOException("That battle code has already been used.");
            }
            if (!MODE_ONE_V_ONE.equalsIgnoreCase(modeOf(battle))) {
                throw new DAOException("That code belongs to a different battle mode.");
            }
            if (battle.getPlayer1Id() == player2Id) {
                throw new DAOException("The second player must enter this code from a different account.");
            }

            battle.setPlayer2Id(player2Id);
            battle.setStatus(STATUS_MATCHED);
            battleDAO.save(battle);
            battleDAO.addParticipant(battle.getId(), battle.getPlayer1Id());
            battleDAO.addParticipant(battle.getId(), player2Id);
            return battle;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to join battle room.");
        }
    }

    public Battle createFreeForAllRoom(int creatorId, String difficulty) {
        try {
            Problem problem = problemDAO.getRandomPublishedByDifficulty(difficulty);
            if (problem == null) {
                throw new DAOException("No published problem found for " + difficulty + ".");
            }

            Battle battle = new Battle();
            battle.setPlayer1Id(creatorId);
            battle.setPlayer2Id(creatorId);
            battle.setProblemId(problem.getId());
            battle.setJoinCode(generateUniqueJoinCode());
            battle.setBattleMode(MODE_FREE_FOR_ALL);
            battle.setStatus("PENDING");
            battle.setTimeLimit(timeLimitForDifficulty(difficulty));
            battleDAO.save(battle);
            battleDAO.addParticipant(battle.getId(), creatorId);
            return battle;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to create free for all room.");
        }
    }

    public Battle joinFreeForAllRoom(String joinCode, int userId) {
        try {
            String normalizedCode = normalizeJoinCode(joinCode);
            if (normalizedCode.isBlank()) {
                throw new DAOException("Enter a battle code.");
            }

            Battle battle = battleDAO.findByJoinCode(normalizedCode);
            if (battle == null) {
                throw new DAOException("No battle room found for that code.");
            }
            if (!MODE_FREE_FOR_ALL.equalsIgnoreCase(modeOf(battle))) {
                throw new DAOException("That code belongs to a different battle mode.");
            }
            if (!"PENDING".equalsIgnoreCase(battle.getStatus())) {
                throw new DAOException("That free for all has already started.");
            }

            battleDAO.addParticipant(battle.getId(), userId);
            return battleDAO.getById(battle.getId());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to join free for all room.");
        }
    }

    public Battle startFreeForAll(Battle battle, int creatorId) {
        try {
            Battle currentBattle = battle == null ? null : battleDAO.getById(battle.getId());
            if (currentBattle == null) {
                throw new DAOException("Free for all room is no longer available.");
            }
            if (!MODE_FREE_FOR_ALL.equalsIgnoreCase(modeOf(currentBattle))) {
                throw new DAOException("This is not a free for all room.");
            }
            if (currentBattle.getPlayer1Id() != creatorId) {
                throw new DAOException("Only the match creator can start this free for all.");
            }
            if (!"PENDING".equalsIgnoreCase(currentBattle.getStatus())) {
                throw new DAOException("This free for all has already started.");
            }
            if (battleDAO.countParticipants(currentBattle.getId()) < 2) {
                throw new DAOException("At least two players must join before starting.");
            }

            currentBattle.setStatus("ACTIVE");
            currentBattle.setStartedAt(LocalDateTime.now().toString());
            battleDAO.save(currentBattle);
            return currentBattle;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to start free for all.");
        }
    }

    public Battle findRandomOneVsOne(int userId, String difficulty) {
        try {
            Battle existingQueue = getPendingRooms(userId, MODE_RANDOM_ONE_V_ONE).stream()
                    .findFirst()
                    .orElse(null);
            if (existingQueue != null) {
                return existingQueue;
            }

            Battle waitingBattle = battleDAO.findQueuedRandomMatch(userId, normalizeDifficultyLabel(difficulty));
            if (waitingBattle != null) {
                waitingBattle.setPlayer2Id(userId);
                waitingBattle.setStatus(STATUS_MATCHED);
                battleDAO.save(waitingBattle);
                battleDAO.addParticipant(waitingBattle.getId(), waitingBattle.getPlayer1Id());
                battleDAO.addParticipant(waitingBattle.getId(), userId);
                return waitingBattle;
            }

            Problem problem = problemDAO.getRandomPublishedByDifficulty(difficulty);
            if (problem == null) {
                throw new DAOException("No published problem found for " + difficulty + ".");
            }

            Battle battle = new Battle();
            battle.setPlayer1Id(userId);
            battle.setPlayer2Id(userId);
            battle.setProblemId(problem.getId());
            battle.setBattleMode(MODE_RANDOM_ONE_V_ONE);
            battle.setStatus("PENDING");
            battle.setTimeLimit(timeLimitForDifficulty(difficulty));
            battleDAO.save(battle);
            battleDAO.addParticipant(battle.getId(), userId);
            return battle;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to find random match.");
        }
    }

    public Battle getBattle(int battleId) {
        try {
            return battleDAO.getById(battleId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load battle.");
        }
    }

    public Problem getBattleProblem(Battle battle) {
        try {
            return problemDAO.getById(battle.getProblemId());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load battle problem.");
        }
    }

    public String getOpponentName(Battle battle, int currentUserId) {
        if (battle == null) {
            return "-";
        }
        if ("PENDING".equalsIgnoreCase(battle.getStatus())) {
            return "Waiting for player 2";
        }
        if (MODE_FREE_FOR_ALL.equalsIgnoreCase(modeOf(battle))) {
            return "Free for all";
        }
        int opponentId = battle.getPlayer1Id() == currentUserId ? battle.getPlayer2Id() : battle.getPlayer1Id();
        User opponent = userDAO.findById(opponentId);
        return opponent == null ? "Unknown user" : opponent.getUsername();
    }

    public boolean isParticipant(Battle battle, int userId) {
        if (battle == null) {
            return false;
        }
        if (MODE_FREE_FOR_ALL.equalsIgnoreCase(modeOf(battle))) {
            return battleDAO.hasParticipant(battle.getId(), userId);
        }
        return battle.getPlayer1Id() == userId || battle.getPlayer2Id() == userId;
    }

    public int getParticipantCount(Battle battle) {
        if (battle == null) {
            return 0;
        }
        if (MODE_FREE_FOR_ALL.equalsIgnoreCase(modeOf(battle))) {
            return battleDAO.countParticipants(battle.getId());
        }
        return STATUS_MATCHED.equalsIgnoreCase(battle.getStatus()) || "ACTIVE".equalsIgnoreCase(battle.getStatus()) ? 2 : 1;
    }

    public Battle markReadyAndStartIfNeeded(Battle battle, int userId) {
        try {
            Battle currentBattle = battle == null ? null : battleDAO.getById(battle.getId());
            if (currentBattle == null) {
                throw new DAOException("Battle is no longer available.");
            }
            if (!isParticipant(currentBattle, userId)) {
                throw new DAOException("Only battle participants can enter this battle.");
            }
            if (!STATUS_MATCHED.equalsIgnoreCase(currentBattle.getStatus())) {
                return currentBattle;
            }

            battleDAO.markParticipantReady(currentBattle.getId(), userId);
            if (battleDAO.countReadyParticipants(currentBattle.getId()) >= requiredReadyCount(currentBattle)) {
                currentBattle.setStatus("ACTIVE");
                currentBattle.setStartedAt(LocalDateTime.now().toString());
                battleDAO.save(currentBattle);
                return currentBattle;
            }
            return battleDAO.getById(currentBattle.getId());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to enter battle.");
        }
    }

    public int getRemainingSeconds(Battle battle) {
        if (battle == null || battle.getStartedAt() == null || !"ACTIVE".equalsIgnoreCase(battle.getStatus())) {
            return 0;
        }

        LocalDateTime startedAt = parseStartedAt(battle.getStartedAt());
        int timeLimit = battle.getTimeLimit() <= 0 ? DEFAULT_TIME_LIMIT_SECONDS : battle.getTimeLimit();
        long elapsed = java.time.Duration.between(startedAt, LocalDateTime.now()).toSeconds();
        return Math.max(0, timeLimit - (int) elapsed);
    }

    public boolean expireIfNeeded(Battle battle) {
        try {
            if (battle == null || !"ACTIVE".equalsIgnoreCase(battle.getStatus())) {
                return false;
            }
            if (getRemainingSeconds(battle) > 0) {
                return false;
            }
            finishAsDraw(battle);
            return true;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to expire battle.");
        }
    }

    public boolean finishWithWinner(Battle battle, int winnerId) {
        try {
            Battle currentBattle = battle == null ? null : battleDAO.getById(battle.getId());
            if (currentBattle == null || isClosed(currentBattle)) {
                return false;
            }
            if (!isParticipant(currentBattle, winnerId)) {
                throw new DAOException("Only battle participants can finish this battle.");
            }

            User winner = userDAO.findById(winnerId);
            User loser = null;
            if (!MODE_FREE_FOR_ALL.equalsIgnoreCase(modeOf(currentBattle))) {
                int loserId = currentBattle.getPlayer1Id() == winnerId ? currentBattle.getPlayer2Id() : currentBattle.getPlayer1Id();
                loser = userDAO.findById(loserId);
            }

            currentBattle.setWinnerId(winnerId);
            currentBattle.setStatus("FINISHED");
            currentBattle.setFinishedAt(LocalDateTime.now().toString());
            battleDAO.save(currentBattle);

            if (winner != null) {
                progressService.awardBattleWin(winner, loser);
            }
            return true;
        } catch (Exception exception) {
            throw wrap(exception, "Failed to finish battle.");
        }
    }

    public void finishAsDraw(Battle battle) {
        try {
            if (battle == null || isClosed(battle)) {
                return;
            }
            battle.setWinnerId(null);
            battle.setStatus("DRAW");
            battle.setFinishedAt(LocalDateTime.now().toString());
            battleDAO.save(battle);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to finish battle as draw.");
        }
    }

    private DAOException wrap(Exception exception, String message) {
        if (exception instanceof DAOException daoException) {
            return daoException;
        }
        return new DAOException(message, exception);
    }

    private boolean isClosed(Battle battle) {
        return "FINISHED".equalsIgnoreCase(battle.getStatus()) || "DRAW".equalsIgnoreCase(battle.getStatus());
    }

    private int requiredReadyCount(Battle battle) {
        if (MODE_FREE_FOR_ALL.equalsIgnoreCase(modeOf(battle))) {
            return battleDAO.countParticipants(battle.getId());
        }
        return 2;
    }

    private LocalDateTime parseStartedAt(String startedAt) {
        try {
            return LocalDateTime.parse(startedAt);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(startedAt, SQLITE_DATE_TIME);
        }
    }

    public int timeLimitForDifficulty(String difficulty) {
        return switch (difficulty == null ? "" : difficulty.trim().toLowerCase(Locale.ROOT)) {
            case "easy" -> 10 * 60;
            case "medium" -> 25 * 60;
            case "hard" -> 45 * 60;
            default -> DEFAULT_TIME_LIMIT_SECONDS;
        };
    }

    public String modeOf(Battle battle) {
        return battle == null || battle.getBattleMode() == null || battle.getBattleMode().isBlank()
                ? MODE_ONE_V_ONE
                : battle.getBattleMode();
    }

    private String generateUniqueJoinCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String code = randomJoinCode();
            if (battleDAO.findByJoinCode(code) == null) {
                return code;
            }
        }
        throw new DAOException("Could not generate a battle code. Try again.");
    }

    private String randomJoinCode() {
        StringBuilder code = new StringBuilder();
        for (int index = 0; index < 6; index++) {
            code.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }

    private String normalizeJoinCode(String joinCode) {
        return joinCode == null ? "" : joinCode.trim().replace(" ", "").toUpperCase(Locale.ROOT);
    }

    private String normalizeDifficultyLabel(String difficulty) {
        return switch (difficulty == null ? "" : difficulty.trim().toLowerCase(Locale.ROOT)) {
            case "easy" -> "Easy";
            case "medium" -> "Medium";
            case "hard" -> "Hard";
            default -> difficulty == null ? "" : difficulty.trim();
        };
    }
}
