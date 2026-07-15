package io.testautomation.booker.booking.update;

import io.testautomation.booker.authentication.TokenProvider;
import io.testautomation.booker.booking.client.BookingClient;
import io.testautomation.booker.booking.data.BookingDataFactory;
import io.testautomation.booker.booking.model.BookingResponse;
import io.testautomation.booker.booking.model.CreateBookingRequest;
import io.testautomation.booker.booking.model.UpdateBookingRequest;
import io.testautomation.booker.booking.workflow.BookingWorkflow;
import io.testautomation.booker.booking.workflow.CreatedBooking;
import io.testautomation.booker.classification.Booker;
import io.testautomation.booker.classification.BookingFeature;
import io.testautomation.booker.reporting.ReportGroup;
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
@ReportGroup("Update booking")
class UpdateBookingTest {
    @Test
    @Smoke
    @Regression
    @QaseId(23)
    @DisplayName("Verify that a booking is fully replaced when a valid payload and auth token are supplied")
    void shouldReplaceBookingWhenPayloadAndTokenAreValid(
            BookingClient client, BookingWorkflow workflow, TokenProvider tokenProvider) {
        Faker faker = new Faker();
        CreateBookingRequest originalRequest = CreateBookingRequest.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(100, 1_000))
                .depositpaid(faker.bool().bool())
                .bookingdates(BookingDataFactory.validDates(faker))
                .additionalneeds(faker.options().option("Breakfast", "Late checkout"))
                .build();
        CreatedBooking created = workflow.create(originalRequest);
        UpdateBookingRequest updateRequest = UpdateBookingRequest.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(1_001, 2_000))
                .depositpaid(!originalRequest.getDepositpaid())
                .bookingdates(BookingDataFactory.validDates(faker))
                .additionalneeds(faker.options().option("Airport transfer", "Late checkout"))
                .build();

        Response response = client.update(created.id(), updateRequest, tokenProvider.token());
        BookingResponse updated = response.as(BookingResponse.class);
        BookingResponse persisted = client.get(created.id()).as(BookingResponse.class);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(updated).usingRecursiveComparison().isEqualTo(updateRequest);
        assertThat(persisted).usingRecursiveComparison().isEqualTo(updateRequest);
        assertThat(persisted).usingRecursiveComparison().isNotEqualTo(originalRequest);
    }

    @Test
    @Regression
    @QaseId(25)
    @DisplayName("Verify that a booking remains unchanged when a full update is attempted without authorization")
    void shouldKeepBookingUnchangedWhenUpdateIsUnauthorized(
            BookingClient client, BookingWorkflow workflow) {
        Faker faker = new Faker();
        CreateBookingRequest originalRequest = CreateBookingRequest.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(100, 1_000))
                .depositpaid(faker.bool().bool())
                .bookingdates(BookingDataFactory.validDates(faker))
                .additionalneeds(faker.options().option("Breakfast", "Late checkout"))
                .build();
        CreatedBooking created = workflow.create(originalRequest);
        UpdateBookingRequest updateRequest = UpdateBookingRequest.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(1_001, 2_000))
                .depositpaid(!originalRequest.getDepositpaid())
                .bookingdates(BookingDataFactory.validDates(faker))
                .additionalneeds("Unauthorized change")
                .build();

        Response response = client.updateWithoutAuthorization(created.id(), updateRequest);
        BookingResponse persisted = client.get(created.id()).as(BookingResponse.class);

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.asString()).doesNotContain("firstname", "lastname", "bookingdates");
        assertThat(persisted).usingRecursiveComparison().isEqualTo(originalRequest);
    }
}
