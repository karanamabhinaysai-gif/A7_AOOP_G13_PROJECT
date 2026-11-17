package com.reeltime;

import java.time.Instant;

public sealed interface SeatState permits SeatState.Free, SeatState.Held, SeatState.Sold {
    record Free() implements SeatState {}
    record Held(String holdId, String bookingId, Instant expiresAt) implements SeatState {}
    record Sold(String bookingId, String ticketId) implements SeatState {}
}
