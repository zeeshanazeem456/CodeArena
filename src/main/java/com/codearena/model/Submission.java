package com.codearena.model;

import com.codearena.judge.Verdict;

public class Submission extends BaseEntity {

    private int userId;
    private int problemId;
    private String code;
    private String language;
    private Verdict verdict;
    private Integer runtimeMs;
    private Integer battleId;
    private String submittedAt;

    public Submission() {
    }

    public Submission(int id, String createdAt, int userId, int problemId, String code, String language,
                      Verdict verdict, Integer runtimeMs, Integer battleId, String submittedAt) {
        setId(id);
        setCreatedAt(createdAt);
        this.userId = userId;
        this.problemId = problemId;
        this.code = code;
        this.language = language;
        this.verdict = verdict;
        this.runtimeMs = runtimeMs;
        this.battleId = battleId;
        this.submittedAt = submittedAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public Integer getRuntimeMs() {
        return runtimeMs;
    }

    public void setRuntimeMs(Integer runtimeMs) {
        this.runtimeMs = runtimeMs;
    }

    public Integer getBattleId() {
        return battleId;
    }

    public void setBattleId(Integer battleId) {
        this.battleId = battleId;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }
}
