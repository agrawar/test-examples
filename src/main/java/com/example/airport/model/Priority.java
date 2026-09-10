package com.example.airport.model;

public enum Priority {
    PRIORITY_1(1),
    PRIORITY_2(2),
    PRIORITY_3(3);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
