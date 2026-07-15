package io.testautomation.demoqa.webtables.model;

public record WebTableRecord(
        String firstName,
        String lastName,
        int age,
        String email,
        int salary,
        String department) {
    public WebTableRecord {
        requireText(firstName, "firstName");
        requireText(lastName, "lastName");
        requireText(email, "email");
        requireText(department, "department");
        if (age < 1 || age > 120) {
            throw new IllegalArgumentException("age must be between 1 and 120");
        }
        if (salary < 0) {
            throw new IllegalArgumentException("salary must not be negative");
        }
    }

    public String value(WebTableColumn column) {
        return switch (column) {
            case FIRST_NAME -> firstName;
            case LAST_NAME -> lastName;
            case AGE -> Integer.toString(age);
            case EMAIL -> email;
            case SALARY -> Integer.toString(salary);
            case DEPARTMENT -> department;
        };
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
