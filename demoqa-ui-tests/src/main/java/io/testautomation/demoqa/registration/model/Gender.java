package io.testautomation.demoqa.registration.model;

public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
