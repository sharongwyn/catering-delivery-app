package com.example.proyekbdpbo.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class cartstorage {
    private static Integer currentBranchId = null;
    private static final ObservableList<cartitem> cartList = FXCollections.observableArrayList();

    public static ObservableList<cartitem> getCartList() {
        return cartList;
    }

    public static void addItem(cartitem item) {
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
        currentBranchId = null;
    }

    public static ObservableList<cartitem> getItems() {
        return cartList;
    }

    public static Integer getCurrentBranchId() {
        return currentBranchId;
    }

    public static void setCurrentBranchId(Integer currentBranchId) {
        cartstorage.currentBranchId = currentBranchId;
    }
}
