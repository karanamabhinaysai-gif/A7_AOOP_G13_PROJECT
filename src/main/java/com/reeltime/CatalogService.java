package com.reeltime;

import java.time.LocalDate;
import java.util.List;

public final class CatalogService {
    private final List<Movie> movies = List.of(
        new Movie("rrr", "RRR", Language.TELUGU, "Action / Drama", "UA 16+", "3h 07m", new Money(240)),
        new Movie("sita", "Sita Ramam", Language.TELUGU, "Romance / Drama", "U", "2h 43m", new Money(220)),
        new Movie("vikram", "Vikram", Language.TAMIL, "Action / Thriller", "UA 16+", "2h 54m", new Money(230)),
        new Movie("ponniyin", "Ponniyin Selvan: I", Language.TAMIL, "Epic / Adventure", "UA 13+", "2h 47m", new Money(220)),
        new Movie("interstellar", "Interstellar", Language.HOLLYWOOD, "Sci-Fi / Adventure", "UA 13+", "2h 49m", new Money(290)),
        new Movie("inception", "Inception", Language.HOLLYWOOD, "Sci-Fi / Thriller", "UA 13+", "2h 28m", new Money(270))
    );
    private final List<String> times = List.of("10:15 AM", "1:30 PM", "4:45 PM", "8:00 PM");

    public List<Movie> movies() { return movies; }
    public List<Show> showsFor(Movie movie) {
        return times.stream().map(time -> new Show(movie.id() + "-" + time.replaceAll("[^0-9]", ""), movie,
            LocalDate.now(), time, "PVR Next Galleria, Hitech City")).toList();
    }
}
