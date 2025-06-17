package com.example.proyekbdpbo.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class cartstorage {
    private static String currentBranch = null;
    private static final ObservableList<cartitem> cartList = FXCollections.observableArrayList();

    public static ObservableList<cartitem> getCartList() {
        return cartList;
    }

    public static void addItem(cartitem item) {
        // Cek apakah menu udah ada di cart
        for (cartitem c : cartList) {
            if (c.getName().equals(item.getName())) {
                c.setQuantity(c.getQuantity() + item.getQuantity());
                return;
            }
        }
        cartList.add(item);
    }

    public static void clearCart() {
        cartList.clear();
    }

    public static ObservableList<cartitem> getItems() {
        return cartList;
    }

    public static String getCurrentBranch() {
        return currentBranch;
    }

    public static void setCurrentBranch(String currentBranch) {
        cartstorage.currentBranch = currentBranch;
    }
}
