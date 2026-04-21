package com.codearena.util;

public final class XPCalculator {

    private XPCalculator() {
    }

    public static int forSolvingProblem(String difficulty) {
        if (difficulty == null) {
            return 0;
        }

        return switch (difficulty.trim().toUpperCase()) {
            case "EASY" -> 10;
            case "MEDIUM" -> 25;
            case "HARD" -> 50;
            default -> 0;
        };
    }

    public static String getRankTitle(int xp) {
        if (xp >= 12000) {
            return "Legend";
        }
        if (xp >= 7000) {
            return "Champion";
        }
        if (xp >= 3500) {
            return "Elite";
        }
        if (xp >= 1500) {
            return "Warrior";
        }
        if (xp >= 500) {
            return "Apprentice";
        }
        return "Novice";
    }
}
