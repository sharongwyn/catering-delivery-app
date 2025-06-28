package com.example.proyekbdpbo.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Delivery {
    private final IntegerProperty id;
    private final StringProperty staffName;
    private final StringProperty jenisKendaraan;
    private final StringProperty platNomor;
    private final StringProperty jam;
    private final StringProperty estimasi;
    private final ObjectProperty<LocalDate> tanggalPengiriman;

    public Delivery(int id, String staffName, String jenisKendaraan, String platNomor,
                    String jam, String estimasi, LocalDate tanggalPengiriman) {
        this.id = new SimpleIntegerProperty(id);
        this.staffName = new SimpleStringProperty(staffName);
        this.jenisKendaraan = new SimpleStringProperty(jenisKendaraan);
        this.platNomor = new SimpleStringProperty(platNomor);
        this.jam = new SimpleStringProperty(jam);
        this.estimasi = new SimpleStringProperty(estimasi);
        this.tanggalPengiriman = new SimpleObjectProperty<>(tanggalPengiriman);
    }

    // Getter untuk value biasa
    public int getId() { return id.get(); }
    public String getStaffName() { return staffName.get(); }
    public String getJenisKendaraan() { return jenisKendaraan.get(); }
    public String getPlatNomor() { return platNomor.get(); }
    public String getJam() { return jam.get(); }
    public String getEstimasi() { return estimasi.get(); }
    public LocalDate getTanggalPengiriman() { return tanggalPengiriman.get(); }

    // Getter untuk property
    public IntegerProperty idProperty() { return id; }
    public StringProperty staffNameProperty() { return staffName; }
    public StringProperty jenisKendaraanProperty() { return jenisKendaraan; }
    public StringProperty platNomorProperty() { return platNomor; }
    public StringProperty jamProperty() { return jam; }
    public StringProperty estimasiProperty() { return estimasi; }
    public ObjectProperty<LocalDate> tanggalPengirimanProperty() { return tanggalPengiriman; }
}
