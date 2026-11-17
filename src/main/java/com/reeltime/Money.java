package com.reeltime;

import java.text.NumberFormat;
import java.util.Locale;

public record Money(long rupees) implements Comparable<Money> {
    public Money plus(Money other) { return new Money(rupees + other.rupees); }
    public static Money zero() { return new Money(0); }
    public String formatted() {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));
        format.setMaximumFractionDigits(0);
        return format.format(rupees);
    }
    @Override public int compareTo(Money other) { return Long.compare(rupees, other.rupees); }
}
