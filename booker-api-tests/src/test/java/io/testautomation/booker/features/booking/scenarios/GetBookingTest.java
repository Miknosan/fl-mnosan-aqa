package io.testautomation.booker.features.booking.scenarios;

import io.testautomation.booker.booking.client.BookingClient;
import io.testautomation.booker.booking.data.BookingDataFactory;
import io.testautomation.booker.booking.model.BookingResponse;
import io.testautomation.booker.booking.model.CreateBookingRequest;
import io.testautomation.booker.booking.workflow.BookingWorkflow;
import io.testautomation.booker.booking.workflow.CreatedBooking;
import io.testautomation.booker.features.booking.BookingFeature;
import io.testautomation.booker.framework.metadata.Booker;
import io.testautomation.booker.framework.metadata.ReportGroup;
import io.testautomation.core.classification.Regression;
import io.testautomation.core.classification.Smoke;
import io.qase.commons.annotation.QaseId;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Booker
@BookingFeature
@ReportGroup("Retrieve booking")
class GetBookingTest {
    @Test
    @Smoke
    @Regression
    @QaseId(13)
    @DisplayName("Verify that a booking is returned when an existing booking ID is requested as JSON")
    void shouldReturnBookingWhenBookingIdExists(BookingClient client, BookingWorkflow workflow) {
        Faker faker = new Faker();
        CreateBookingRequest request = CreateBookingRequest.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(100, 1_000))
                .depositpaid(faker.bool().bool())
                .bookingdates(BookingDataFactory.validDates(faker))
                .additionalneeds(faker.options().option("Breakfast", "Late checkout"))
                .build();
        CreatedBooking created = workflow.create(request);

        Response response = client.get(created.id());
        BookingResponse booking = response.as(BookingResponse.class);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.contentType()).startsWith("application/json");
        assertThat(booking).usingRecursiveComparison().isEqualTo(request);
    }

    @Test
    @Regression
    @QaseId(15)
    @DisplayName("Verify that a not-found response is returned when a non-existent booking ID is requested")
    void shouldReturnNotFoundWhenBookingIdDoesNotExist(BookingClient client, BookingWorkflow workflow) {
        int bookingId = workflow.nonExistingBookingId();

        Response response = client.get(bookingId);

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.asString()).doesNotContain("firstname", "lastname", "bookingdates");
    }
}
