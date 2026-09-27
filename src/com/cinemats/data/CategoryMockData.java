package com.cinemats.data;

import com.cinemats.model.Category;

import java.util.ArrayList;
import java.util.List;

// Default movie categories mock data
public final class CategoryMockData {

    private CategoryMockData() {}

    // Returns default movie categories
    public static List<Category> getInitialCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(1, "Action", "High-energy sequences, stunts, pursuits, and physical conflicts", "2026-09-01 10:00:00"));
        categories.add(new Category(2, "Adventure", "Exciting journeys, expeditions, heroic quests, and exploration", "2026-09-01 10:00:00"));
        categories.add(new Category(3, "Animation", "CGI, 3D, and hand-drawn animated films for all audiences", "2026-09-01 10:00:00"));
        categories.add(new Category(4, "Comedy", "Lighthearted humor, satirical plots, and comedic entertainment", "2026-09-01 10:00:00"));
        categories.add(new Category(5, "Crime", "Detective investigations, criminal syndicates, and forensic drama", "2026-09-01 10:00:00"));
        categories.add(new Category(6, "Drama", "Character-driven realistic stories, conflicts, and deep emotion", "2026-09-01 10:00:00"));
        categories.add(new Category(7, "Fantasy", "Mythological realms, magical powers, folklore, and mythical creatures", "2026-09-01 10:00:00"));
        categories.add(new Category(8, "Horror", "Supernatural mysteries, psychological fear, and eerie atmosphere", "2026-09-01 10:00:00"));
        categories.add(new Category(9, "Romance", "Love stories, passionate relationships, and intimate journeys", "2026-09-01 10:00:00"));
        categories.add(new Category(10, "Sci-Fi", "Futuristic technology, space exploration, time travel, and AI", "2026-09-01 10:00:00"));
        categories.add(new Category(11, "Thriller", "High-stakes tension, psychological suspense, and unexpected twists", "2026-09-01 10:00:00"));
        return categories;
    }
}
