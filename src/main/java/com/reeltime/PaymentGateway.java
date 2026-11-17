package com.reeltime;

public interface PaymentGateway {
    boolean charge(String bookingId, Money amount);
}
