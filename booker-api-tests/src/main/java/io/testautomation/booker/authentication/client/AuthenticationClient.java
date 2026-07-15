package io.testautomation.booker.authentication.client;

import io.testautomation.booker.authentication.model.AuthenticationRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

import static io.restassured.RestAssured.given;

public final class AuthenticationClient {
    private final RequestSpecification specification;

    public AuthenticationClient(RequestSpecification specification) {
        this.specification = Objects.requireNonNull(specification, "specification");
    }

    public Response authenticate(AuthenticationRequest request) {
        return given().spec(specification).body(request).post("/auth");
    }
}
