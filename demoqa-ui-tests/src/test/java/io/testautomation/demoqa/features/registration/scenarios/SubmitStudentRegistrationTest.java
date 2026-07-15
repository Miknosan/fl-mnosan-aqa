package io.testautomation.demoqa.features.registration.scenarios;

import io.qase.commons.annotation.QaseId;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.testautomation.demoqa.features.registration.RegistrationFeature;
import io.testautomation.demoqa.framework.metadata.DemoQa;
import io.testautomation.demoqa.framework.metadata.ReportGroup;
import io.testautomation.demoqa.registration.data.StudentRegistrationDataFactory;
import io.testautomation.demoqa.registration.model.RegistrationResult;
import io.testautomation.demoqa.registration.model.StudentRegistrationData;
import io.testautomation.demoqa.registration.page.StudentRegistrationPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.testautomation.demoqa.features.registration.StudentRegistrationAssertions.assertSuccessfulRegistration;

@DemoQa
@RegistrationFeature
@ReportGroup("Submit registration")
class SubmitStudentRegistrationTest {
    @Test
    @Smoke
    @Regression
    @QaseId(44)
    @DisplayName("[UI] Verify that the student registration is completed when all supported fields contain valid data")
    void shouldCompleteStudentRegistrationWhenAllSupportedFieldsContainValidData(
            StudentRegistrationPage registrationPage,
            StudentRegistrationDataFactory dataFactory) {
        StudentRegistrationData registration = dataFactory.completeRegistration();

        registrationPage.open();
        registrationPage.complete(registration);
        registrationPage.submit();

        RegistrationResult result = registrationPage.resultModal().result();
        assertSuccessfulRegistration(registration, registrationPage.resultModal().title(), result);
    }
}
