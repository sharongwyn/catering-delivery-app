package com.example.proyekbdpbo.model;

public class branch {
    private int id;
    private String name;
    private double avgRating;
    private String alamat;
    private String telp;

    public branch(int id, String name, double avgRating, String alamat, String telp) {
        this.id = id;
        this.name = name;
        this.avgRating = avgRating;
        this.alamat = alamat;
        this.telp = telp;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public double getAvgRating() { return avgRating; }

    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }

    @Override
    public String toString() {
        return name;
    }
}
