package io.testautomation.booker.features.booking.scenarios;

import io.testautomation.booker.booking.client.BookingClient;
import io.testautomation.booker.booking.data.BookingDataFactory;
import io.testautomation.booker.booking.model.BookingDates;
import io.testautomation.booker.booking.model.CreateBookingRequest;
import io.testautomation.booker.features.booking.BookingFeature;
import io.testautomation.booker.framework.metadata.Booker;
import io.testautomation.booker.framework.metadata.ReportGroup;
import io.testautomation.core.classification.Regression;
import io.qase.commons.annotation.QaseId;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@Booker
@BookingFeature
@ReportGroup("Create booking validation")
class CreateBookingValidationTest {
    @ParameterizedTest(name = "[{0}]")
    @EnumSource(MandatoryField.class)
    @Regression
    @QaseId(9)
    @DisplayName("Verify that booking creation is rejected when a mandatory field is missing")
    void shouldRejectBookingCreationWhenMandatoryFieldIsMissing(
            MandatoryField missingField, BookingClient client) {
        Faker faker = new Faker();
        BookingDates dates = BookingDataFactory.validDates(faker);
        CreateBookingRequest.CreateBookingRequestBuilder builder = CreateBookingRequest.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(100, 1_000))
                .depositpaid(faker.bool().bool())
                .bookingdates(dates)
                .additionalneeds(faker.options().option("Breakfast", "Late checkout"));

        switch (missingField) {
            case FIRSTNAME -> builder.firstname(null);
            case LASTNAME -> builder.lastname(null);
            case TOTAL_PRICE -> builder.totalprice(null);
            case DEPOSIT_PAID -> builder.depositpaid(null);
            case CHECKIN -> builder.bookingdates(BookingDates.builder()
                    .checkout(dates.getCheckout()).build());
            case CHECKOUT -> builder.bookingdates(BookingDates.builder()
                    .checkin(dates.getCheckin()).build());
        }

        Response response = client.create(builder.build());

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.asString()).doesNotContain("bookingid");
    }

    enum MandatoryField {
        FIRSTNAME("firstname"),
        LASTNAME("lastname"),
        TOTAL_PRICE("total price"),
        DEPOSIT_PAID("deposit paid"),
        CHECKIN("check-in"),
        CHECKOUT("check-out");

        private final String displayName;

        MandatoryField(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
