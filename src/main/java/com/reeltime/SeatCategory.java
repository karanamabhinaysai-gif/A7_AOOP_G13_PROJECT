package com.reeltime;

public sealed interface SeatCategory permits SeatCategory.Regular, SeatCategory.Premium, SeatCategory.Recliner {
    String label();
    Money surcharge();

    enum Regular implements SeatCategory {
        INSTANCE;
        public String label() { return "Classic"; }
        public Money surcharge() { return Money.zero(); }
    }
    enum Premium implements SeatCategory {
        INSTANCE;
        public String label() { return "Premium"; }
        public Money surcharge() { return new Money(70); }
    }
    enum Recliner implements SeatCategory {
        INSTANCE;
        public String label() { return "Recliner"; }
        public Money surcharge() { return new Money(170); }
    }

    static SeatCategory forRow(char row) {
        if (row <= 'B') return Recliner.INSTANCE;
        if (row <= 'D') return Premium.INSTANCE;
        return Regular.INSTANCE;
    }
}
