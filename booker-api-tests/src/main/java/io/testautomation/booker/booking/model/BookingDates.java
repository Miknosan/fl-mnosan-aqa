package io.testautomation.booker.booking.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Value
@Builder
@Jacksonized
public class BookingDates {
    LocalDate checkin;
    LocalDate checkout;
}
