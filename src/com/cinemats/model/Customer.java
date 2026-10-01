package com.cinemats.model;

import java.io.Serializable;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String phone;
    private String email;
    private String createdAt;

    public Customer() {
    }

    public static String capitalize(String str) {
        if (str == null || str.trim().isEmpty()) return "";
        String[] parts = str.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) {
                    sb.append(p.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    public Customer(String name, String phone) {
        this.name = capitalize(name);
        this.phone = phone;
    }

    public Customer(int id, String name, String phone, String email, String createdAt) {
        this.id = id;
        this.name = capitalize(name);
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return capitalize(name);
    }

    public void setName(String name) {
        this.name = capitalize(name);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}

