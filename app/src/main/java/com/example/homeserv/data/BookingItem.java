package com.example.homeserv.data;

public class BookingItem {
    public final int id;
    public final int offerId;
    public final String serviceTitle;
    public final String customerName;
    public final String providerName;
    public final double price;
    public final String dateTime;
    public final String status;
    public final String notes;

    public BookingItem(int id, int offerId, String serviceTitle, String customerName,
                       String providerName, double price, String dateTime,
                       String status, String notes) {
        this.id = id;
        this.offerId = offerId;
        this.serviceTitle = serviceTitle;
        this.customerName = customerName;
        this.providerName = providerName;
        this.price = price;
        this.dateTime = dateTime;
        this.status = status;
        this.notes = notes;
    }
}
