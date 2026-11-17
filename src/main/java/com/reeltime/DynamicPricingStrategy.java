package com.reeltime;

public interface DynamicPricingStrategy {
    Money price(Show show, SeatCategory category);
}
