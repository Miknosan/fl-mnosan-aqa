package io.testautomation.demoqa.features.registration;

import io.testautomation.demoqa.registration.model.Gender;
import io.testautomation.demoqa.registration.model.RegistrationFormState;
import io.testautomation.demoqa.registration.model.RegistrationResult;
import io.testautomation.demoqa.registration.model.RequiredRegistrationField;
import io.testautomation.demoqa.registration.model.StudentRegistrationData;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public final class StudentRegistrationAssertions {
    private static final DateTimeFormatter RESULT_DATE =
            DateTimeFormatter.ofPattern("dd MMMM,yyyy", Locale.ENGLISH);

    private StudentRegistrationAssertions() {
    }

    public static void assertSuccessfulRegistration(
            StudentRegistrationData data,
            String modalTitle,
            RegistrationResult result) {
        assertThat(modalTitle).isEqualTo("Thanks for submitting the form");
        assertThat(result.values()).containsExactlyInAnyOrderEntriesOf(expectedResult(data));
    }

    public static void assertRequiredValuesRetained(
            StudentRegistrationData data,
            RequiredRegistrationField omitted,
            RegistrationFormState actual) {
        RegistrationFormState expected = new RegistrationFormState(
                omitted == RequiredRegistrationField.FIRST_NAME ? "" : data.firstName(),
                omitted == RequiredRegistrationField.LAST_NAME ? "" : data.lastName(),
                omitted == RequiredRegistrationField.GENDER ? null : data.gender(),
                omitted == RequiredRegistrationField.MOBILE ? "" : data.mobile());
        assertThat(actual).isEqualTo(expected);
    }

    private static Map<String, String> expectedResult(StudentRegistrationData data) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("Student Name", data.firstName() + " " + data.lastName());
        values.put("Student Email", data.email());
        values.put("Gender", data.gender().displayName());
        values.put("Mobile", data.mobile());
        values.put("Date of Birth", RESULT_DATE.format(data.dateOfBirth()));
        values.put("Subjects", String.join(", ", data.subjects()));
        values.put("Hobbies", data.hobbies().stream()
                .map(io.testautomation.demoqa.registration.model.Hobby::displayName)
                .collect(Collectors.joining(", ")));
        values.put("Picture", data.picture().name());
        values.put("Address", data.currentAddress());
        values.put("State and City", data.state() + " " + data.city());
        return values;
    }
}
