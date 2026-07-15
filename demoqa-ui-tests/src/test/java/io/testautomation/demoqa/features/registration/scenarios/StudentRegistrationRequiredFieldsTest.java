package io.testautomation.demoqa.features.registration.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.demoqa.features.registration.RegistrationFeature;
import io.testautomation.demoqa.framework.metadata.DemoQa;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import io.testautomation.demoqa.registration.data.StudentRegistrationDataFactory;
import io.testautomation.demoqa.registration.model.RequiredRegistrationField;
import io.testautomation.demoqa.registration.model.StudentRegistrationData;
import io.testautomation.demoqa.registration.page.StudentRegistrationPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static io.testautomation.demoqa.features.registration.StudentRegistrationAssertions.assertRequiredValuesRetained;
import static org.assertj.core.api.Assertions.assertThat;

@DemoQa
@RegistrationFeature
@ReportGroup("Registration validation")
class StudentRegistrationRequiredFieldsTest {
    @ParameterizedTest(name = "Missing required field: {0}")
    @EnumSource(RequiredRegistrationField.class)
    @Regression
    @QaseId(45)
    @DisplayName("[UI] Verify that the student registration is blocked when a required field is missing")
    void shouldBlockStudentRegistrationWhenRequiredFieldIsMissing(
            RequiredRegistrationField missingField,
            StudentRegistrationPage registrationPage,
            StudentRegistrationDataFactory dataFactory) {
        StudentRegistrationData registration = dataFactory.requiredRegistration().without(missingField);

        registrationPage.open();
        registrationPage.complete(registration);
        registrationPage.submit();

        assertThat(registrationPage.resultModal().isVisible()).isFalse();
        assertThat(registrationPage.isInvalid(missingField)).isTrue();
        assertRequiredValuesRetained(registration, missingField, registrationPage.requiredState());
    }
}
