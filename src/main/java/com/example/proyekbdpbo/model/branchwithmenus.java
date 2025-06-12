package com.example.proyekbdpbo.model;

import java.util.ArrayList;

public class branchwithmenus {
    private int id;
    private String name;
    private ArrayList<menu> menus;

    public branchwithmenus(String name) {
        this.name = name;
        this.menus = new ArrayList<>();

    }

    public void addMenu(menu m) {
        this.menus.add(m);
    }

    public ArrayList<menu> getMenus() {
        return menus;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name; // supaya tampil di choicebox as string, karena kalau ngga pakai ini bakal munculnya @234blabla
    }
}
