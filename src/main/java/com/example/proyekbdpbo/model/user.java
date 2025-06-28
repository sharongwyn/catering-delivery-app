package com.example.proyekbdpbo.model;

public class user {
    private String username;
    private String nama;

    public user(String username, String nama) {
        this.username = username;
        this.nama = nama;

    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getNama() { return nama; }

    public void setNama(String nama) { this.nama = nama; }
}
