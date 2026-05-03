package com.codearena.model;

public class Badge extends BaseEntity {

    private String code;
    private String name;
    private String description;
    private String category;
    private String imagePath;
    private int sortOrder;
    private boolean active;
    private String earnedAt;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(String earnedAt) {
        this.earnedAt = earnedAt;
    }

    public boolean isEarned() {
        return earnedAt != null && !earnedAt.isBlank();
    }
}
