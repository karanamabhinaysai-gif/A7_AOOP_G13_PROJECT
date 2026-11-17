package com.reeltime;

import java.time.Instant;
import java.util.List;

public record Booking(String id, String ticketId, Show show, List<String> seats, String orderDescription,
                      Money total, Instant bookedAt) {
    public Booking { seats = List.copyOf(seats); }
}
