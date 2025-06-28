package com.example.proyekbdpbo.model;

import javafx.beans.property.ObjectProperty;

import java.time.LocalDate;

public class orderData {
    private final int idOrder;
    private final String namaPelanggan;
    private final double totalHarga;
    private final String deliverySchedule;
    private final int idPengiriman;
    private final String namaStatus;
    private final LocalDate tanggalPengiriman;


    public orderData(int idOrder, String namaPelanggan, double totalHarga,
                     String deliverySchedule, int idPengiriman, String namaStatus, LocalDate tanggalPengiriman) {
        this.idOrder = idOrder;
        this.namaPelanggan = namaPelanggan;
        this.totalHarga = totalHarga;
        this.deliverySchedule = deliverySchedule;
        this.idPengiriman = idPengiriman;
        this.namaStatus = namaStatus;
        this.tanggalPengiriman = tanggalPengiriman;
    }

    public int getIdOrder() { return idOrder; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public double getTotalHarga() { return totalHarga; }
    public String getDeliverySchedule() { return deliverySchedule; }
    public int getIdPengiriman() { return idPengiriman; }
    public String getNamaStatus() { return namaStatus; }
    public LocalDate getTanggalPengiriman() {
        return tanggalPengiriman;
    }
}
