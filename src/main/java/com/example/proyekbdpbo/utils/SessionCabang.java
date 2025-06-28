package com.example.proyekbdpbo.utils;

public class SessionCabang {
    private static int idCabang = -1;
    private static int idAdminCabang = -1; // opsional

    public static void setIdCabang(int id) {
        idCabang = id;
    }

    public static int getIdCabang() {
        return idCabang;
    }

    public static void clear() {
        idCabang = -1;
        idAdminCabang = -1;
    }

    // Tambahan jika butuh simpan ID admin
    public static void setIdAdminCabang(int id) {
        idAdminCabang = id;
    }

    public static int getIdAdminCabang() {
        return idAdminCabang;
    }
}
