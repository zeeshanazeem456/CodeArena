package com.codearena.service;

import com.codearena.dao.ProblemDAO;
import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.TestCaseDAO;
import com.codearena.dao.UserDAO;
import com.codearena.model.Problem;
import com.codearena.model.Submission;
import com.codearena.model.TestCase;
import com.codearena.model.User;
import java.util.List;
import com.codearena.util.DAOException;

public class AdminService {

    private final UserDAO userDAO;
    private final ProblemDAO problemDAO;
    private final TestCaseDAO testCaseDAO;
    private final SubmissionDAO submissionDAO;

    public AdminService() {
        this(new UserDAO(), new ProblemDAO(), new TestCaseDAO(), new SubmissionDAO());
    }

    public AdminService(UserDAO userDAO, ProblemDAO problemDAO, TestCaseDAO testCaseDAO, SubmissionDAO submissionDAO) {
        this.userDAO = userDAO;
        this.problemDAO = problemDAO;
        this.testCaseDAO = testCaseDAO;
        this.submissionDAO = submissionDAO;
    }

    public List<User> getUsers() {
        return userDAO.getAll();
    }

    public void setUserActive(int userId, boolean active) {
        userDAO.setActive(userId, active);
    }

    public List<Problem> getProblems() {
        return problemDAO.getAllIncludingDrafts();
    }

    public void saveProblem(Problem problem) {
        validateProblem(problem);
        problemDAO.save(problem);
    }

    public void deleteProblem(int problemId) {
        submissionDAO.deleteByProblemId(problemId);
        testCaseDAO.deleteByProblemId(problemId);
        problemDAO.delete(problemId);
    }

    public List<TestCase> getTestCases(int problemId) {
        return testCaseDAO.getByProblemId(problemId);
    }

    public void saveTestCase(TestCase testCase) {
        validateTestCase(testCase);
        testCaseDAO.save(testCase);
    }

    public void deleteTestCase(int testCaseId) {
        testCaseDAO.delete(testCaseId);
    }

    public List<Submission> getSubmissions() {
        return submissionDAO.findAll();
    }

    public String getUsername(int userId) {
        User user = userDAO.findById(userId);
        return user == null ? "User #" + userId : user.getUsername();
    }

    public String getProblemTitle(int problemId) {
        Problem problem = problemDAO.getById(problemId);
        return problem == null ? "Problem #" + problemId : problem.getTitle();
    }

    public void deleteSubmission(int submissionId) {
        submissionDAO.delete(submissionId);
    }

    public void validateProblemReadyForSave(Problem problem, List<TestCase> testCases) {
        validateProblem(problem);
        if (testCases == null || testCases.isEmpty()) {
            throw new DAOException("Add at least one test case before saving.");
        }
        testCases.forEach(this::validateTestCase);
    }

    private void validateProblem(Problem problem) {
        if (problem == null) {
            throw new DAOException("Problem data is required.");
        }
        if (isBlank(problem.getTitle())) {
            throw new DAOException("Title is required.");
        }
        if (isBlank(problem.getDescription())) {
            throw new DAOException("Problem statement is required.");
        }
        if (problem.getDifficulty() == null) {
            throw new DAOException("Difficulty is required.");
        }
    }

    private void validateTestCase(TestCase testCase) {
        if (testCase == null) {
            throw new DAOException("Test case data is required.");
        }
        if (isBlank(testCase.getInput())) {
            throw new DAOException("Test case input is required.");
        }
        if (isBlank(testCase.getExpected())) {
            throw new DAOException("Expected output is required.");
        }
        if (testCase.getSequenceOrder() < 1) {
            throw new DAOException("Sequence number must be at least 1.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
