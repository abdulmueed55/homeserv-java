package com.example.homeserv.data;

public class DashboardCounts {
    public final int totalOffers;
    public final int totalProviders;
    public final int totalBookings;

    public DashboardCounts(int totalOffers, int totalProviders, int totalBookings) {
        this.totalOffers = totalOffers;
        this.totalProviders = totalProviders;
        this.totalBookings = totalBookings;
    }
}
