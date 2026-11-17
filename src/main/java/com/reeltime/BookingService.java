package com.reeltime;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Booking context orchestrates hold → payment → sale; payment failure releases the hold. */
public final class BookingService {
    private final Map<String, SeatInventory> inventories = new ConcurrentHashMap<>();
    private final PaymentGateway payments;
    private final DynamicPricingStrategy pricing;
    private final List<Booking> bookings = Collections.synchronizedList(new ArrayList<>());

    public BookingService(PaymentGateway payments, DynamicPricingStrategy pricing) {
        this.payments = payments; this.pricing = pricing;
    }

    public SeatInventory inventoryFor(Show show) {
        return inventories.computeIfAbsent(show.id(), ignored -> new SeatInventory(show));
    }

    public Booking book(Show show, List<String> seats, Set<String> comboIds) {
        SeatInventory inventory = inventoryFor(show);
        String bookingId = UUID.randomUUID().toString();
        String holdId = UUID.randomUUID().toString();
        String ticketId = "RT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        if (!inventory.holdSeats(seats, holdId, bookingId, Duration.ofMinutes(4)))
            throw new IllegalStateException("One or more seats are unavailable. Please choose again.");
        Orderable order = TicketOrder.forSeats(show, seats, pricing);
        if (comboIds.contains("popcorn")) order = new PopcornCombo(order);
        if (comboIds.contains("nachos")) order = new NachosCombo(order);
        if (comboIds.contains("sweet")) order = new SweetCombo(order);
        Money total = order.total().plus(new Money(Math.round(order.total().rupees() * 0.035)));
        try {
            if (!payments.charge(bookingId, total)) throw new IllegalStateException("Payment was declined.");
            if (!inventory.sellHeldSeats(seats, holdId, bookingId, ticketId))
                throw new IllegalStateException("The seat hold expired before confirmation; payment needs compensation.");
            Booking booking = new Booking(bookingId, ticketId, show, seats, order.description(), total, Instant.now());
            bookings.add(booking);
            return booking;
        } catch (RuntimeException failure) {
            inventory.releaseHold(holdId);
            throw failure;
        }
    }

    public List<Booking> bookings() { synchronized (bookings) { return List.copyOf(bookings); } }
}
