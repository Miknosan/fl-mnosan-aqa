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
class StudentRegistrationMobileValidationTest {
    @Test
    @Regression
    @QaseId(47)
    @DisplayName("[UI] Verify that the student registration is blocked when the mobile number has fewer than ten digits")
    void shouldBlockStudentRegistrationWhenMobileNumberHasFewerThanTenDigits(
            StudentRegistrationPage registrationPage,
            StudentRegistrationDataFactory dataFactory) {
        StudentRegistrationData registration = dataFactory.requiredRegistration().withMobile("987654321");

        registrationPage.open();
        registrationPage.complete(registration);
        registrationPage.submit();

        assertThat(registrationPage.resultModal().isVisible()).isFalse();
        assertThat(registrationPage.isMobileInvalid()).isTrue();
        assertThat(registrationPage.requiredState().firstName()).isEqualTo(registration.firstName());
        assertThat(registrationPage.requiredState().lastName()).isEqualTo(registration.lastName());
        assertThat(registrationPage.requiredState().gender()).isEqualTo(registration.gender());
        assertThat(registrationPage.requiredState().mobile()).isEqualTo(registration.mobile());
    }
}
