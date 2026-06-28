package com.example.homeserv.data;

public class Provider {
    public final int id;
    public final String name;
    public final String phone;
    public final String serviceType;
    public final Integer userId; // nullable

    public Provider(int id, String name, String phone, String serviceType, Integer userId) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.serviceType = serviceType;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return name + " - " + serviceType;
    }
}
