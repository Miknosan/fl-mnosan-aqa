package io.testautomation.booker.features.booking.scenarios;

import io.testautomation.booker.authentication.TokenProvider;
import io.testautomation.booker.booking.client.BookingClient;
import io.testautomation.booker.booking.data.BookingDataFactory;
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
@ReportGroup("Delete booking")
class DeleteBookingTest {
    @Test
    @Smoke
    @Regression
    @QaseId(33)
    @DisplayName("Verify that a booking is removed when deletion is requested with a valid auth token")
    void shouldDeleteBookingWhenTokenIsValid(
            BookingClient client, BookingWorkflow workflow, TokenProvider tokenProvider) {
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
        assertThat(client.get(created.id()).statusCode()).isEqualTo(200);

        Response deleteResponse = client.delete(created.id(), tokenProvider.token());
        Response getResponse = client.get(created.id());

        assertThat(deleteResponse.statusCode()).isEqualTo(201);
        assertThat(getResponse.statusCode()).isEqualTo(404);
    }
}
