package com.codearena.judge;

public enum Verdict {
    AC,
    WA,
    TLE,
    CE,
    RE,
    MLE;

    public int priority() {
        return switch (this) {
            case CE -> 5;
            case RE -> 4;
            case TLE -> 3;
            case WA -> 2;
            case MLE -> 1;
            case AC -> 0;
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case AC -> "Accepted";
            case WA -> "Wrong Answer";
            case TLE -> "Time Limit Exceeded";
            case CE -> "Compilation Error";
            case RE -> "Runtime Error";
            case MLE -> "Memory Limit Exceeded";
        };
    }

    public String getColor() {
        return switch (this) {
            case AC -> "#3FB950";
            case WA -> "#F85149";
            case TLE -> "#D29922";
            case CE -> "#BC8CFF";
            case RE -> "#FF6B6B";
            case MLE -> "#D29922";
        };
    }

    public boolean isTerminal() {
        return this == CE;
    }
}
