package io.testautomation.booker.authentication;

import io.testautomation.booker.authentication.client.AuthenticationClient;
import io.testautomation.booker.authentication.model.AuthenticationRequest;
import io.testautomation.booker.authentication.model.AuthenticationResponse;
import io.testautomation.booker.classification.AuthenticationFeature;
import io.testautomation.booker.classification.Booker;
import io.testautomation.booker.reporting.ReportGroup;
import io.testautomation.core.classification.Regression;
import io.qase.commons.annotation.QaseId;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Booker
@AuthenticationFeature
@ReportGroup("Credential validation")
class AuthenticationValidationTest {
    @Test
    @Regression
    @QaseId(3)
    @DisplayName("Verify that authentication is rejected when the password is invalid")
    void shouldRejectAuthenticationWhenPasswordIsInvalid(AuthenticationClient client) {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("admin")
                .password("wrong-password")
                .build();

        Response response = client.authenticate(request);
        AuthenticationResponse authentication = response.as(AuthenticationResponse.class);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(authentication.getToken()).isNull();
        assertThat(authentication.getReason()).isEqualTo("Bad credentials");
    }
}
