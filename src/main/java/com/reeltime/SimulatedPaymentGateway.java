package com.reeltime;

public final class SimulatedPaymentGateway implements PaymentGateway {
    @Override public boolean charge(String bookingId, Money amount) {
        return true; // Demo only: no real payment is taken.
    }
}
