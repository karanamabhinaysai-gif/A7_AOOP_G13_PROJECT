package com.reeltime;

public record Movie(String id, String title, Language language, String genre, String rating,
                    String duration, Money basePrice) {
    @Override public String toString() { return title + "  ·  " + language.label() + "  ·  " + rating; }
}
