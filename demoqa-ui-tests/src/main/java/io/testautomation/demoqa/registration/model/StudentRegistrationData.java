package io.testautomation.demoqa.registration.model;

import java.time.LocalDate;
import java.util.List;

public record StudentRegistrationData(
        String firstName,
        String lastName,
        String email,
        Gender gender,
        String mobile,
        LocalDate dateOfBirth,
        List<String> subjects,
        List<Hobby> hobbies,
        UploadedFile picture,
        String currentAddress,
        String state,
        String city) {
    public StudentRegistrationData {
        firstName = valueOrEmpty(firstName);
        lastName = valueOrEmpty(lastName);
        email = valueOrEmpty(email);
        mobile = valueOrEmpty(mobile);
        subjects = subjects == null ? List.of() : List.copyOf(subjects);
        hobbies = hobbies == null ? List.of() : List.copyOf(hobbies);
        currentAddress = valueOrEmpty(currentAddress);
        state = valueOrEmpty(state);
        city = valueOrEmpty(city);
    }

    public StudentRegistrationData without(RequiredRegistrationField field) {
        return switch (field) {
            case FIRST_NAME -> copy("", lastName, email, gender, mobile);
            case LAST_NAME -> copy(firstName, "", email, gender, mobile);
            case GENDER -> copy(firstName, lastName, email, null, mobile);
            case MOBILE -> copy(firstName, lastName, email, gender, "");
        };
    }

    public StudentRegistrationData withEmail(String value) {
        return copy(firstName, lastName, value, gender, mobile);
    }

    public StudentRegistrationData withMobile(String value) {
        return copy(firstName, lastName, email, gender, value);
    }

    private StudentRegistrationData copy(
            String newFirstName,
            String newLastName,
            String newEmail,
            Gender newGender,
            String newMobile) {
        return new StudentRegistrationData(
                newFirstName,
                newLastName,
                newEmail,
                newGender,
                newMobile,
                dateOfBirth,
                subjects,
                hobbies,
                picture,
                currentAddress,
                state,
                city);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
