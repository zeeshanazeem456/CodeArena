package com.codearena.model;

public class TestCase {

    private int id;
    private int problemId;
    private String input;
    private String expected;
    private boolean sample;

    public TestCase() {
    }

    public TestCase(int id, int problemId, String input, String expected, boolean sample) {
        this.id = id;
        this.problemId = problemId;
        this.input = input;
        this.expected = expected;
        this.sample = sample;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public boolean isSample() {
        return sample;
    }

    public void setSample(boolean sample) {
        this.sample = sample;
    }
}
