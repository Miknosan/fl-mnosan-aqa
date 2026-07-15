package io.testautomation.demoqa.registration.model;

public enum Hobby {
    SPORTS("Sports"),
    READING("Reading"),
    MUSIC("Music");

    private final String displayName;

    Hobby(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
