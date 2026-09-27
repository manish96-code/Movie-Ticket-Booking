package com.cinemats.model;

import java.util.Objects;

/**
 * Category entity model representing film genres and classifications.
 */
public class Category {
    private final int id;
    private final String name;
    private final String description;
    private final String createdAt;

    public Category(String name, String description) {
        this(0, name, description, "");
    }

    public Category(int id, String name, String description, String createdAt) {
        this.id = id;
        this.name = (name == null) ? "" : name.trim();
        this.description = (description == null) ? "" : description.trim();
        this.createdAt = (createdAt == null) ? "" : createdAt.trim();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return name.equalsIgnoreCase(category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }
}
