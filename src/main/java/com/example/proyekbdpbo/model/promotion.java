package com.example.proyekbdpbo.model;

import java.sql.Date;

public class promotion {
    private int id;
    private double discount; // potongan_promo (misalnya 0.2)
    private Date startDate;  // tanggal_promoBerlaku
    private Date endDate;    // tanggal_promoBerakhir

    // Constructor
    public promotion(int id, double discount, Date startDate, Date endDate) {
        this.id = id;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Constructor tanpa ID (untuk insert baru sebelum ID di-generate DB)
    public promotion(double discount, Date startDate, Date endDate) {
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters dan Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
