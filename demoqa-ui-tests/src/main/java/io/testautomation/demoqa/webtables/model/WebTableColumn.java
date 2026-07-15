package io.testautomation.demoqa.webtables.model;

public enum WebTableColumn {
    FIRST_NAME("First Name", 0, false),
    LAST_NAME("Last Name", 1, false),
    AGE("Age", 2, true),
    EMAIL("Email", 3, false),
    SALARY("Salary", 4, true),
    DEPARTMENT("Department", 5, false);

    private final String header;
    private final int index;
    private final boolean numeric;

    WebTableColumn(String header, int index, boolean numeric) {
        this.header = header;
        this.index = index;
        this.numeric = numeric;
    }

    public String header() {
        return header;
    }

    public int index() {
        return index;
    }

    public boolean numeric() {
        return numeric;
    }
}
