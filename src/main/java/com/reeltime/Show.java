package com.reeltime;

import java.time.LocalDate;

public record Show(String id, Movie movie, LocalDate date, String time, String cinema) {
    @Override public String toString() { return movie.title() + "  ·  " + time + "  ·  " + cinema; }
}
