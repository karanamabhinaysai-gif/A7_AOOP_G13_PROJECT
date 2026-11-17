package com.reeltime;

import java.util.List;

public record TicketOrder(Money total, String description) implements Orderable {
    public static TicketOrder forSeats(Show show, List<String> seats, DynamicPricingStrategy pricing) {
        Money amount = Money.zero();
        for (String seat : seats) amount = amount.plus(pricing.price(show, SeatCategory.forRow(seat.charAt(0))));
        return new TicketOrder(amount, seats.size() + " ticket(s)");
    }
}
