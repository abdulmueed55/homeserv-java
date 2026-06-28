package com.example.homeserv.data;

public class Offer {
    public final int id;
    public final int providerId;
    public final String providerName;
    public final String serviceType;
    public final String title;
    public final String description;
    public final double price;
    public final String duration;

    public Offer(int id, int providerId, String providerName, String serviceType,
                 String title, String description, double price, String duration) {
        this.id = id;
        this.providerId = providerId;
        this.providerName = providerName;
        this.serviceType = serviceType;
        this.title = title;
        this.description = description;
        this.price = price;
        this.duration = duration;
    }
}
