package com.reeltime;

public abstract class ComboDecorator implements Orderable {
    protected final Orderable order;
    private final String item;
    private final Money price;
    protected ComboDecorator(Orderable order, String item, Money price) { this.order = order; this.item = item; this.price = price; }
    @Override public Money total() { return order.total().plus(price); }
    @Override public String description() { return order.description() + " + " + item; }
}
