package com.reeltime;

public final class PopcornCombo extends ComboDecorator {
    public PopcornCombo(Orderable order) { super(order, "Classic popcorn combo", new Money(320)); }
}
