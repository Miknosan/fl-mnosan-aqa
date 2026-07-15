package io.testautomation.booker.booking.data;

import io.testautomation.booker.booking.model.BookingDates;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.Objects;

public final class BookingDataFactory {
    private BookingDataFactory() {
    }

    public static BookingDates validDates(Faker faker) {
        Objects.requireNonNull(faker, "faker");
        LocalDate checkin = LocalDate.now().plusDays(faker.number().numberBetween(10, 60));
        LocalDate checkout = checkin.plusDays(faker.number().numberBetween(1, 14));
        return BookingDates.builder().checkin(checkin).checkout(checkout).build();
    }
}
