package io.testautomation.booker.booking.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class BookingIdResponse {
    int bookingid;
}
