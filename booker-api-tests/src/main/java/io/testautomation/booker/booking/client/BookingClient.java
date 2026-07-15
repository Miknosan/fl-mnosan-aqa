package io.testautomation.booker.booking.client;

import io.testautomation.booker.booking.model.CreateBookingRequest;
import io.testautomation.booker.booking.model.UpdateBookingRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static io.restassured.RestAssured.given;

public final class BookingClient {
    private final RequestSpecification specification;

    public BookingClient(RequestSpecification specification) {
        this.specification = Objects.requireNonNull(specification, "specification");
    }

    public Response create(CreateBookingRequest request) {
        return given().spec(specification).body(request).post("/booking");
    }

    public Response get(int bookingId) {
        return given().spec(specification).get("/booking/{id}", bookingId);
    }

    public Response getIds() {
        return given().spec(specification).get("/booking");
    }

    public Response findByGuestName(String firstname, String lastname) {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put("firstname", firstname);
        queryParameters.put("lastname", lastname);
        return given().spec(specification).queryParams(queryParameters).get("/booking");
    }

    public Response update(int bookingId, UpdateBookingRequest request, String token) {
        return given().spec(specification)
                .cookie("token", token)
                .body(request)
                .put("/booking/{id}", bookingId);
    }

    public Response updateWithoutAuthorization(int bookingId, UpdateBookingRequest request) {
        return given().spec(specification).body(request).put("/booking/{id}", bookingId);
    }

    public Response delete(int bookingId, String token) {
        return given().spec(specification)
                .cookie("token", token)
                .delete("/booking/{id}", bookingId);
    }
}
