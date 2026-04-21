package com.codearena.model;

public class Problem extends BaseEntity {

    private String title;
    private String description;
    private Difficulty difficulty;
    private String category;
    private String tags;
    private int timeLimit;
    private int memoryLimit;
    private boolean isPublished;

    public Problem() {
    }

    public Problem(int id, String createdAt, String title, String description, Difficulty difficulty, String category,
                   String tags, int timeLimit, int memoryLimit, boolean isPublished) {
        setId(id);
        setCreatedAt(createdAt);
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.category = category;
        this.tags = tags;
        this.timeLimit = timeLimit;
        this.memoryLimit = memoryLimit;
        this.isPublished = isPublished;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }
}
