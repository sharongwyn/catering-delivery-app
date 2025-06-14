package com.example.proyekbdpbo.model;

public class menu {
    private String name;
    private String imagePath;

    public menu(String name, String imageFileName) {
        this.name = name;
//        var url = getClass().getResource("/images/" + imageFileName);
//        if (url == null) {
//            throw new RuntimeException("Gambar tidak ditemukan: " + imageFileName);
//        }
//        this.imagePath = url.toExternalForm();
        this.imagePath = imageFileName;

    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getImagePath() { return imagePath; }

    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
