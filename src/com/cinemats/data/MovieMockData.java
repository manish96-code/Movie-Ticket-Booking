package com.cinemats.data;

import com.cinemats.model.Movie;

import java.util.ArrayList;
import java.util.List;

// Default movies catalogue mock data
public final class MovieMockData {

    private MovieMockData() {}

    // Returns default initial movies with authentic theatrical posters
    public static List<Movie> getInitialMovies() {
        List<Movie> movies = new ArrayList<>();
        movies.add(new Movie(1, "Interstellar", "Sci-Fi / Adventure", 169, "UA 13+", "INTERSTELLAR", "NOW_SHOWING", "assets/posters/poster_interstellar.jpg", "English", "2014-11-07"));
        movies.add(new Movie(2, "Dune: Part Two", "Action / Adventure", 166, "UA 13+", "DUNE: PART TWO", "NOW_SHOWING", "assets/posters/poster_dune2.jpg", "English", "2024-03-01"));
        movies.add(new Movie(3, "Oppenheimer", "Biography / Drama", 180, "A", "OPPENHEIMER", "NOW_SHOWING", "assets/posters/poster_oppenheimer.jpg", "English", "2023-07-21"));
        movies.add(new Movie(4, "Spider-Man: Across The Spider-Verse", "Animation / Action", 140, "U", "SPIDER-MAN", "NOW_SHOWING", "assets/posters/poster_spiderman.jpg", "English", "2023-06-02"));
        movies.add(new Movie(5, "Inception", "Sci-Fi / Thriller", 148, "UA 13+", "INCEPTION", "NOW_SHOWING", "assets/posters/poster_inception.jpg", "English", "2010-07-16"));
        movies.add(new Movie(6, "The Dark Knight", "Action / Crime", 152, "UA 13+", "THE DARK KNIGHT", "NOW_SHOWING", "assets/posters/poster_dark_knight.jpg", "English", "2008-07-18"));
        movies.add(new Movie(28, "Jawan", "Action / Thriller", 169, "UA 13+", "JAWAN", "NOW_SHOWING", "assets/posters/poster_jawan.jpg", "Hindi", "2023-09-07"));
        movies.add(new Movie(29, "Kalki 2898 AD", "Sci-Fi / Action", 181, "UA 13+", "KALKI 2898 AD", "NOW_SHOWING", "assets/posters/poster_kalki.jpg", "Hindi", "2024-06-27"));
        movies.add(new Movie(30, "Stree 2", "Comedy / Horror", 147, "UA 13+", "STREE 2", "NOW_SHOWING", "assets/posters/poster_stree2.jpg", "Hindi", "2024-08-15"));
        movies.add(new Movie(31, "Deadpool & Wolverine", "Action / Comedy", 128, "A", "DEADPOOL & WOLVERINE", "NOW_SHOWING", "assets/posters/poster_deadpool_wolverine.jpg", "English", "2024-07-26"));
        movies.add(new Movie(32, "Fighter", "Action / Thriller", 166, "UA 13+", "FIGHTER", "NOW_SHOWING", "assets/posters/poster_fighter.jpg", "Hindi", "2024-01-25"));
        movies.add(new Movie(33, "Avatar: The Way of Water", "Sci-Fi / Adventure", 192, "UA 13+", "AVATAR: WATER", "NOW_SHOWING", "assets/posters/poster_avatar_water.jpg", "English", "2022-12-16"));
        movies.add(new Movie(34, "Pushpa 2: The Rule", "Action / Drama", 200, "UA 13+", "PUSHPA 2", "UPCOMING", "assets/posters/poster_pushpa2.jpg", "Hindi", "2026-12-05"));
        movies.add(new Movie(35, "Singham Again", "Action / Crime", 160, "UA 13+", "SINGHAM AGAIN", "UPCOMING", "assets/posters/poster_singham_again.jpg", "Hindi", "2026-11-01"));
        movies.add(new Movie(36, "Gladiator II", "Action / Drama", 148, "A", "GLADIATOR II", "UPCOMING", "assets/posters/poster_gladiator2.jpg", "English", "2026-11-22"));
        movies.add(new Movie(37, "Avengers: Doomsday", "Action / Sci-Fi", 180, "UA 13+", "AVENGERS: DOOMSDAY", "UPCOMING", "assets/posters/poster_avengers_doomsday.jpg", "English", "2026-05-01"));
        movies.add(new Movie(38, "RRR", "Action / Drama", 187, "UA 13+", "RRR", "NOW_SHOWING", "assets/posters/poster_rrr.jpg", "Telugu", "2022-03-25"));
        movies.add(new Movie(39, "Top Gun: Maverick", "Action / Drama", 130, "UA 13+", "TOP GUN", "NOW_SHOWING", "assets/posters/poster_top_gun_maverick.jpg", "English", "2022-05-27"));
        return movies;
    }
}
