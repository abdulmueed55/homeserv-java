package com.example.homeserv.data;

public class User {
    public final int id;
    public final String name;
    public final String phone;
    public final String role;
    public final boolean isBlocked;

    public User(int id, String name, String phone, String role, boolean isBlocked) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.isBlocked = isBlocked;
    }
}
