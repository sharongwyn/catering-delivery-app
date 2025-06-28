package com.example.proyekbdpbo.model;

public class orderItem {
    private int no;
    private String menuName;
    private int quantity;
    private double price;

    public orderItem(int no, String menuName, int quantity, double price) {
        this.no = no;
        this.menuName = menuName;
        this.quantity = quantity;
        this.price = price;
    }

    public int getNo() { return no; }
    public String getMenuName() { return menuName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}
