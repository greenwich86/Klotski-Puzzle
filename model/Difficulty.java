package model;

public enum Difficulty {
    EASY("Easy"),
    HARD("Hard"),
    EXPERT("Expert"),
    MASTER("Master");

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 