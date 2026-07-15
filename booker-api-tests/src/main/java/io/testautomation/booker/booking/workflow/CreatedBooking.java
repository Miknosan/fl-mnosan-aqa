package io.testautomation.booker.booking.workflow;

import io.testautomation.booker.booking.model.BookingResponse;

public record CreatedBooking(int id, BookingResponse booking) {
}
