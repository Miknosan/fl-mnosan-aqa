package io.testautomation.booker.features.booking.scenarios;

import io.testautomation.booker.booking.client.BookingClient;
import io.testautomation.booker.booking.data.BookingDataFactory;
import io.testautomation.booker.booking.model.BookingResponse;
import io.testautomation.booker.booking.model.CreateBookingRequest;
import io.testautomation.booker.booking.model.CreateBookingResponse;
import io.testautomation.booker.booking.workflow.BookingWorkflow;
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
@ReportGroup("Create booking")
class CreateBookingTest {
    @Test
    @Smoke
    @Regression
    @QaseId(5)
    @DisplayName("Verify that a booking is created when a valid complete payload is submitted")
    void shouldCreateBookingWhenPayloadIsValid(BookingClient client, BookingWorkflow workflow) {
        Faker faker = new Faker();
        CreateBookingRequest request = CreateBookingRequest.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(100, 1_000))
                .depositpaid(faker.bool().bool())
                .bookingdates(BookingDataFactory.validDates(faker))
                .additionalneeds(faker.options().option("Breakfast", "Late checkout", "Airport transfer"))
                .build();

        Response response = client.create(request);
        CreateBookingResponse created = response.as(CreateBookingResponse.class);
        workflow.register(created.getBookingid());
        BookingResponse persisted = client.get(created.getBookingid()).as(BookingResponse.class);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.contentType()).startsWith("application/json");
        assertThat(created.getBookingid()).isPositive();
        assertThat(created.getBooking()).usingRecursiveComparison().isEqualTo(request);
        assertThat(persisted).usingRecursiveComparison().isEqualTo(request);
    }
}
