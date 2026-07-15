package io.testautomation.demoqa.features.registration.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.demoqa.features.registration.RegistrationFeature;
import io.testautomation.demoqa.framework.metadata.DemoQa;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import io.testautomation.demoqa.registration.data.StudentRegistrationDataFactory;
import io.testautomation.demoqa.registration.model.StudentRegistrationData;
import io.testautomation.demoqa.registration.page.StudentRegistrationPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DemoQa
@RegistrationFeature
@ReportGroup("Registration validation")
class StudentRegistrationEmailValidationTest {
    @Test
    @Regression
    @QaseId(46)
    @DisplayName("[UI] Verify that the student registration is blocked when the email format is invalid")
    void shouldBlockStudentRegistrationWhenEmailFormatIsInvalid(
            StudentRegistrationPage registrationPage,
            StudentRegistrationDataFactory dataFactory) {
        StudentRegistrationData registration = dataFactory.requiredRegistration()
                .withEmail("olivia.bennett.example.com");

        registrationPage.open();
        registrationPage.complete(registration);
        registrationPage.submit();

        assertThat(registrationPage.resultModal().isVisible()).isFalse();
        assertThat(registrationPage.isEmailInvalid()).isTrue();
        assertThat(registrationPage.requiredState().firstName()).isEqualTo(registration.firstName());
        assertThat(registrationPage.requiredState().lastName()).isEqualTo(registration.lastName());
        assertThat(registrationPage.requiredState().gender()).isEqualTo(registration.gender());
        assertThat(registrationPage.requiredState().mobile()).isEqualTo(registration.mobile());
    }
}
