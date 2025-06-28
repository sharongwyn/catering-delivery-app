package com.example.proyekbdpbo.utils;

public class Session {
    private static int idPelanggan;
    private static String namaPelanggan;

    public static void setUser(int id, String nama) {
        idPelanggan = id;
        namaPelanggan = nama;
    }

    public static int getIdPelanggan() {
        return idPelanggan;
    }

    public static String getNamaPelanggan() {
        return namaPelanggan;
    }

    public static void clear() {
        idPelanggan = 0;
        namaPelanggan = null;
    }
}
