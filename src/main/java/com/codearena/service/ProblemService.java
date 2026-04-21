package com.codearena.service;

import com.codearena.dao.ProblemDAO;
import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.TestCaseDAO;
import com.codearena.model.Problem;
import com.codearena.model.TestCase;
import com.codearena.util.DAOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProblemService {

    private final ProblemDAO problemDAO;
    private final SubmissionDAO submissionDAO;
    private final TestCaseDAO testCaseDAO;

    public ProblemService() {
        this(new ProblemDAO(), new SubmissionDAO(), new TestCaseDAO());
    }

    public ProblemService(ProblemDAO problemDAO, SubmissionDAO submissionDAO, TestCaseDAO testCaseDAO) {
        this.problemDAO = problemDAO;
        this.submissionDAO = submissionDAO;
        this.testCaseDAO = testCaseDAO;
    }

    public List<Problem> getAllProblems() {
        try {
            return problemDAO.getAll();
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load problems.");
        }
    }

    public List<Problem> filterByDifficulty(String difficulty) {
        try {
            if (difficulty == null || difficulty.trim().isEmpty() || "All".equalsIgnoreCase(difficulty.trim())) {
                return problemDAO.getAll();
            }
            return problemDAO.filterByDifficulty(difficulty);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to filter problems by difficulty.");
        }
    }

    public List<Problem> searchByTitle(String keyword) {
        try {
            return problemDAO.filterByTitle(keyword);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to search problems by title.");
        }
    }

    public boolean isSolvedByUser(int problemId, int userId) {
        try {
            return submissionDAO.hasAcceptedSubmission(problemId, userId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to determine solved status.");
        }
    }

    public List<TestCase> getSampleTestCases(int problemId) {
        try {
            return testCaseDAO.getSampleByProblemId(problemId);
        } catch (Exception exception) {
            throw wrap(exception, "Failed to load sample test cases.");
        }
    }

    public List<Problem> getFilteredProblems(String keyword, String difficulty) {
        try {
            List<Problem> titleMatches = searchByTitle(keyword);

            if (difficulty == null || difficulty.trim().isEmpty() || "All".equalsIgnoreCase(difficulty.trim())) {
                return titleMatches;
            }

            Set<Integer> difficultyMatchedIds = filterByDifficulty(difficulty).stream()
                    .map(Problem::getId)
                    .collect(Collectors.toSet());

            return titleMatches.stream()
                    .filter(problem -> difficultyMatchedIds.contains(problem.getId()))
                    .collect(Collectors.toList());
        } catch (Exception exception) {
            throw wrap(exception, "Failed to apply problem filters.");
        }
    }

    private DAOException wrap(Exception exception, String message) {
        if (exception instanceof DAOException daoException) {
            return daoException;
        }
        return new DAOException(message, exception);
    }
}
