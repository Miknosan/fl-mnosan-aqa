package io.testautomation.booker.features.booking.scenarios;

import io.testautomation.booker.booking.client.BookingClient;
import io.testautomation.booker.booking.data.BookingDataFactory;
import io.testautomation.booker.booking.model.BookingIdResponse;
import io.testautomation.booker.booking.model.BookingResponse;
import io.testautomation.booker.booking.model.CreateBookingRequest;
import io.testautomation.booker.booking.workflow.BookingWorkflow;
import io.testautomation.booker.booking.workflow.CreatedBooking;
import io.testautomation.booker.features.booking.BookingFeature;
import io.testautomation.booker.framework.metadata.Booker;
import io.testautomation.booker.framework.metadata.ReportGroup;
import io.testautomation.core.classification.Regression;
import io.qase.commons.annotation.QaseId;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Booker
@BookingFeature
@ReportGroup("Search bookings")
class SearchBookingTest {
    @Test
    @Regression
    @QaseId(19)
    @DisplayName("Verify that bookings are filtered when matching first and last names are supplied")
    void shouldFilterBookingsWhenGuestNameMatches(BookingClient client, BookingWorkflow workflow) {
        Faker faker = new Faker();
        String matchingFirstname = "Match" + faker.number().digits(8);
        String matchingLastname = "Guest" + faker.number().digits(8);
        CreateBookingRequest matchingRequest = CreateBookingRequest.builder()
                .firstname(matchingFirstname)
                .lastname(matchingLastname)
                .totalprice(faker.number().numberBetween(100, 1_000))
                .depositpaid(faker.bool().bool())
                .bookingdates(BookingDataFactory.validDates(faker))
                .additionalneeds(faker.options().option("Breakfast", "Late checkout"))
                .build();
        CreateBookingRequest nonMatchingRequest = CreateBookingRequest.builder()
                .firstname("Other" + faker.number().digits(8))
                .lastname("Guest" + faker.number().digits(8))
                .totalprice(faker.number().numberBetween(100, 1_000))
                .depositpaid(faker.bool().bool())
                .bookingdates(BookingDataFactory.validDates(faker))
                .additionalneeds(faker.options().option("Breakfast", "Late checkout"))
                .build();
        CreatedBooking matching = workflow.create(matchingRequest);
        CreatedBooking nonMatching = workflow.create(nonMatchingRequest);

        Response response = client.findByGuestName(matchingFirstname, matchingLastname);
        List<BookingIdResponse> results = response.as(new TypeRef<>() { });

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(results).extracting(BookingIdResponse::getBookingid)
                .contains(matching.id())
                .doesNotContain(nonMatching.id());
        assertThat(results).allSatisfy(result -> {
            BookingResponse booking = client.get(result.getBookingid()).as(BookingResponse.class);
            assertThat(booking.getFirstname()).isEqualTo(matchingFirstname);
            assertThat(booking.getLastname()).isEqualTo(matchingLastname);
        });
    }
}
