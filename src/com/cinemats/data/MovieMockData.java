package com.cinemats.data;

import com.cinemats.model.Movie;

import java.util.ArrayList;
import java.util.List;

// Default movies catalogue mock data
public final class MovieMockData {

    private MovieMockData() {}

    // Returns default initial movies without price
    public static List<Movie> getInitialMovies() {
        List<Movie> movies = new ArrayList<>();
        movies.add(new Movie(1, "Interstellar", "Sci-Fi / Adventure", 169, "UA", "INTERSTELLAR", "NOW_SHOWING", "assets/posters/poster_1790658889465.png", "English", "2014-11-07"));
        movies.add(new Movie(2, "Dune: Part Two", "Action / Adventure", 166, "UA", "DUNE: PART TWO", "NOW_SHOWING", "assets/posters/poster_1790659899159.png", "English", "2024-03-01"));
        movies.add(new Movie(3, "Oppenheimer", "Biography / Drama", 180, "A", "OPPENHEIMER", "NOW_SHOWING", "", "English", "2023-07-21"));
        movies.add(new Movie(4, "Spider-Man: Across The Spider-Verse", "Animation / Action", 140, "U", "SPIDER-MAN", "NOW_SHOWING", "assets/posters/poster_1790660557420.png", "English", "2023-06-02"));
        movies.add(new Movie(5, "Inception", "Sci-Fi / Thriller", 148, "UA", "INCEPTION", "NOW_SHOWING", "assets/posters/poster_1790661742295.png", "English", "2010-07-16"));
        movies.add(new Movie(6, "The Dark Knight", "Action / Crime", 152, "UA", "THE DARK KNIGHT", "NOW_SHOWING", "assets/posters/poster_1790757144233.png", "English", "2008-07-18"));
        movies.add(new Movie(28, "Jawan", "Action / Thriller", 169, "UA", "JAWAN", "NOW_SHOWING", "", "Hindi", "2023-09-07"));
        movies.add(new Movie(29, "Kalki 2898 AD", "Sci-Fi / Action", 181, "UA", "KALKI 2898 AD", "NOW_SHOWING", "", "Hindi", "2024-06-27"));
        movies.add(new Movie(30, "Stree 2", "Comedy / Horror", 147, "UA", "STREE 2", "NOW_SHOWING", "", "Hindi", "2024-08-15"));
        movies.add(new Movie(31, "Deadpool & Wolverine", "Action / Comedy", 128, "A", "DEADPOOL & WOLVERINE", "NOW_SHOWING", "", "English", "2024-07-26"));
        movies.add(new Movie(32, "Fighter", "Action / Thriller", 166, "UA", "FIGHTER", "NOW_SHOWING", "", "Hindi", "2024-01-25"));
        movies.add(new Movie(33, "Avatar: The Way of Water", "Sci-Fi / Adventure", 192, "UA", "AVATAR: WATER", "NOW_SHOWING", "", "English", "2022-12-16"));
        movies.add(new Movie(34, "Pushpa 2: The Rule", "Action / Drama", 200, "UA", "PUSHPA 2", "UPCOMING", "", "Hindi", "2026-12-05"));
        movies.add(new Movie(35, "Singham Again", "Action / Crime", 160, "UA", "SINGHAM AGAIN", "UPCOMING", "", "Hindi", "2026-11-01"));
        movies.add(new Movie(36, "Gladiator II", "Action / Drama", 148, "A", "GLADIATOR II", "UPCOMING", "", "English", "2026-11-22"));
        movies.add(new Movie(37, "Avengers: Secret Wars", "Action / Sci-Fi", 180, "UA", "AVENGERS: SECRET WARS", "UPCOMING", "", "English", "2027-05-07"));
        return movies;
    }
}
