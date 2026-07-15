package io.testautomation.booker.booking.workflow;

import io.testautomation.booker.authentication.TokenProvider;
import io.testautomation.booker.booking.client.BookingClient;
import io.testautomation.booker.booking.model.BookingIdResponse;
import io.testautomation.booker.booking.model.CreateBookingRequest;
import io.testautomation.booker.booking.model.CreateBookingResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BookingWorkflow {
    private final BookingClient client;
    private final TokenProvider tokenProvider;
    private final List<Integer> createdBookingIds = new ArrayList<>();

    public BookingWorkflow(BookingClient client, TokenProvider tokenProvider) {
        this.client = Objects.requireNonNull(client, "client");
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider");
    }

    public CreatedBooking create(CreateBookingRequest request) {
        CreateBookingResponse response = client.create(request).as(CreateBookingResponse.class);
        register(response.getBookingid());
        return new CreatedBooking(response.getBookingid(), response.getBooking());
    }

    public void register(int bookingId) {
        createdBookingIds.add(bookingId);
    }

    public int nonExistingBookingId() {
        List<BookingIdResponse> ids = client.getIds().as(new TypeRef<>() { });
        int candidate = Integer.MAX_VALUE;
        while (contains(ids, candidate)) {
            candidate--;
        }
        return candidate;
    }

    public void cleanup() {
        for (int bookingId : createdBookingIds) {
            Response response = client.delete(bookingId, tokenProvider.token());
            int status = response.statusCode();
            if (status != 201 && status != 404 && status != 405) {
                throw new IllegalStateException("Cannot clean up booking " + bookingId + ": HTTP " + status);
            }
        }
        createdBookingIds.clear();
    }

    private static boolean contains(List<BookingIdResponse> ids, int candidate) {
        return ids.stream().anyMatch(id -> id.getBookingid() == candidate);
    }
}
