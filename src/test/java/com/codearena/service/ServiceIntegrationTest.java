package com.codearena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codearena.TestDatabase;
import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.UserDAO;
import com.codearena.judge.Verdict;
import com.codearena.model.Difficulty;
import com.codearena.model.Problem;
import com.codearena.model.Submission;
import com.codearena.model.TestCase;
import com.codearena.model.User;
import com.codearena.util.AuthException;
import com.codearena.util.DAOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceIntegrationTest {

    @BeforeEach
    void resetDatabase() throws Exception {
        TestDatabase.reset();
    }

    @Test
    void authRegistersLogsInAndRejectsInactiveUsers() {
        AuthService auth = new AuthService();

        assertTrue(auth.register("newcoder", "newcoder@codearena.test", "admin123"));
        User loggedIn = auth.login("newcoder", "admin123");

        assertEquals("newcoder", loggedIn.getUsername());
        assertEquals("CODER", loggedIn.getRole());
        assertThrows(AuthException.class, () -> auth.register("tiny", "tiny@codearena.test", "short"));

        new UserDAO().setActive(loggedIn.getId(), false);
        assertThrows(AuthException.class, () -> auth.login("newcoder", "admin123"));
    }

    @Test
    void problemServiceFiltersSearchesAndLoadsSampleTests() throws Exception {
        ProblemService problems = new ProblemService();

        assertFalse(problems.getAllProblems().isEmpty());
        assertTrue(problems.filterByDifficulty("Hard").stream()
                .allMatch(problem -> problem.getDifficulty() == Difficulty.HARD));
        assertTrue(problems.getFilteredProblems("Prime", "Hard", "math").stream()
                .anyMatch(problem -> "Prime Counter".equals(problem.getTitle())));

        int fizzBuzzId = TestDatabase.findProblemId("FizzBuzz");
        assertFalse(problems.getSampleTestCases(fizzBuzzId).isEmpty());
    }

    @Test
    void adminValidationRequiresProblemDataAndTestCases() {
        AdminService admin = new AdminService();
        Problem problem = new Problem();
        problem.setTitle("Test Problem");
        problem.setDescription("Read one value.");
        problem.setDifficulty(Difficulty.EASY);
        problem.setTimeLimit(5);
        problem.setMemoryLimit(256);
        problem.setPublished(true);

        assertThrows(DAOException.class, () -> admin.validateProblemReadyForSave(problem, java.util.List.of()));

        TestCase testCase = new TestCase();
        testCase.setProblemId(1);
        testCase.setInput("1");
        testCase.setExpected("1");
        testCase.setSample(true);
        testCase.setSequenceOrder(1);

        admin.validateProblemReadyForSave(problem, java.util.List.of(testCase));
        admin.saveProblem(problem);

        assertTrue(problem.getId() > 0);
    }

    @Test
    void badgeServiceAwardsNewMilestonesFromExistingActivity() throws Exception {
        AuthService auth = new AuthService();
        auth.register("badgecoder", "badgecoder@codearena.test", "admin123");
        User user = auth.login("badgecoder", "admin123");
        SubmissionDAO submissions = new SubmissionDAO();
        int[] acceptedProblemIds = {
                TestDatabase.findProblemId("Two Sum"),
                TestDatabase.findProblemId("Reverse a String"),
                TestDatabase.findProblemId("FizzBuzz"),
                TestDatabase.findProblemId("Maximum in Array"),
                TestDatabase.findProblemId("Prime Counter")
        };

        for (int problemId : acceptedProblemIds) {
            Submission submission = new Submission();
            submission.setUserId(user.getId());
            submission.setProblemId(problemId);
            submission.setCode("class Solution {}");
            submission.setLanguage("Java");
            submission.setVerdict(Verdict.AC);
            submission.setRuntimeMs(1);
            submissions.save(submission);
        }

        new BadgeService().syncUserBadges(user);

        assertTrue(TestDatabase.userHasBadge(user.getId(), "SOLVE_5"));
        assertTrue(TestDatabase.userHasBadge(user.getId(), "JAVA_5"));
        assertTrue(TestDatabase.userHasBadge(user.getId(), "FIRST_MEDIUM"));
        assertTrue(TestDatabase.userHasBadge(user.getId(), "FIRST_HARD"));
    }

    @Test
    void userProgressUpdatesProblemAndBattleStats() {
        AuthService auth = new AuthService();
        auth.register("winner", "winner@codearena.test", "admin123");
        auth.register("loser", "loser@codearena.test", "admin123");
        User winner = auth.login("winner", "admin123");
        User loser = auth.login("loser", "admin123");

        UserProgressService progress = new UserProgressService();
        progress.awardProblemSolved(winner, 1, Difficulty.MEDIUM);
        User afterSolve = new UserDAO().findById(winner.getId());

        assertEquals(1, afterSolve.getProblemsSolved());
        assertTrue(afterSolve.getXp() > winner.getXp());

        progress.awardBattleWin(afterSolve, loser);
        User afterBattle = new UserDAO().findById(winner.getId());
        User refreshedLoser = new UserDAO().findById(loser.getId());

        assertEquals(1, afterBattle.getBattlesWon());
        assertEquals(1, refreshedLoser.getBattlesLost());
    }

    @Test
    void squadServiceSupportsCreateJoinAndLeaderboard() {
        AuthService auth = new AuthService();
        auth.register("leader", "leader@codearena.test", "admin123");
        auth.register("member", "member@codearena.test", "admin123");
        User leader = auth.login("leader", "admin123");
        User member = auth.login("member", "admin123");
        SquadService squads = new SquadService();

        var squad = squads.createSquad(leader, "Bit Forge", "Serious practice.");
        squads.joinSquad(member, "Bit Forge");

        assertNotNull(squad);
        assertEquals(2, squads.getMembers(squad).size());
        assertTrue(squads.getSquadLeaderboard().stream().anyMatch(item -> "Bit Forge".equals(item.getName())));
    }
}
