package com.codearena.service;

import com.codearena.dao.ProblemDAO;
import com.codearena.dao.SubmissionDAO;
import com.codearena.dao.UserDAO;
import com.codearena.model.Problem;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AnalyticsService {

    private final UserDAO userDAO;
    private final SubmissionDAO submissionDAO;
    private final ProblemDAO problemDAO;

    public AnalyticsService() {
        this(new UserDAO(), new SubmissionDAO(), new ProblemDAO());
    }

    public AnalyticsService(UserDAO userDAO, SubmissionDAO submissionDAO, ProblemDAO problemDAO) {
        this.userDAO = userDAO;
        this.submissionDAO = submissionDAO;
        this.problemDAO = problemDAO;
    }

    public int getTotalUsers() {
        return userDAO.getAll().size();
    }

    public int getTotalSubmissionsToday() {
        return submissionDAO.countToday();
    }

    public Map<String, Integer> getSubmissionVerdictBreakdown() {
        return submissionDAO.verdictBreakdown();
    }

    public List<Problem> getProblemSuccessRates() {
        return problemDAO.getAllIncludingDrafts().stream()
                .sorted(Comparator.comparingDouble((Problem problem) -> problemDAO.getAcceptanceRate(problem.getId())).reversed())
                .toList();
    }

    public double getAcceptanceRate(int problemId) {
        return problemDAO.getAcceptanceRate(problemId);
    }
}
