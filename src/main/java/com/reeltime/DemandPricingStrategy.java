package com.reeltime;

public final class DemandPricingStrategy implements DynamicPricingStrategy {
    @Override public Money price(Show show, SeatCategory category) {
        // Demo demand adjustment; replace with an inventory-demand calculation in production.
        long demandAdjustment = show.time().startsWith("8:") ? 20 : 0;
        return show.movie().basePrice().plus(category.surcharge()).plus(new Money(demandAdjustment));
    }
}
