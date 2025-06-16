package com.example.proyekbdpbo.model;

public class menu {
    private String name;
    private String imagePath;
    private double price;
    private String description;

    public menu(String name, String imageFileName) {
        this.name = name;
//        var url = getClass().getResource("/images/" + imageFileName);
//        if (url == null) {
//            throw new RuntimeException("Gambar tidak ditemukan: " + imageFileName);
//        }
//        this.imagePath = url.toExternalForm();
        this.imagePath = imageFileName;

    }

    public menu(String name, String imageFileName, double price) {
        this.name = name;
        this.imagePath = imageFileName;
        this.price = price;

    }

    public menu(String name, String imageFileName, double price, String description) {
        this.name = name;
        this.imagePath = imageFileName;
        this.price = price;
        this.description = description;

    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getImagePath() { return imagePath; }

    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
