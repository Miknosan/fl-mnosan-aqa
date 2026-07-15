package io.testautomation.demoqa.registration.model;

public record RegistrationFormState(
        String firstName,
        String lastName,
        Gender gender,
        String mobile) {
}
