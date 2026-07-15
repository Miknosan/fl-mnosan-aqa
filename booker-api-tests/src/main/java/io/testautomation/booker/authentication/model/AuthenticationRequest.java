package io.testautomation.booker.authentication.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AuthenticationRequest {
    String username;
    String password;
}
