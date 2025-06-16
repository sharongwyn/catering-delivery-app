package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.model.cartitem;
import com.example.proyekbdpbo.model.cartstorage;
import com.example.proyekbdpbo.model.menu;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javafx.scene.image.Image;
import javafx.event.ActionEvent;

public class permenuController {
    @FXML
    private ImageView itemimage;

    @FXML
    private Label itemname;

    @FXML
    private Label itemdesc;

    @FXML
    private Label itemprice;

    @FXML
    private TextField quantityField;

    @FXML
    private Button addcartButton;

    @FXML
    private Button backButton;

    private menu currentMenu;

    public void setMenu(menu m) {
        this.currentMenu = m;

        // Set UI
        itemname.setText(m.getName());
        itemprice.setText("Rp " + m.getPrice());
        itemdesc.setText(m.getDescription()); // update kalau kamu punya deskripsi di DB
        try {
            itemimage.setImage(new Image(getClass().getResourceAsStream("/images/" + m.getImagePath())));
        } catch (Exception e) {
            System.out.println("Gagal load gambar: " + m.getImagePath());
        }
    }

    @FXML
    public void initialize() {
        addcartButton.setOnAction(e -> handleAddToCart());
        backButton.setOnAction(this::handleBack);
    }

    private void handleAddToCart() {
        if (currentMenu == null) return;

        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) return;

        try {
            int quantity = Integer.parseInt(qtyText);
            if (quantity <= 0) return;

            cartitem item = new cartitem(currentMenu.getName(), quantity, currentMenu.getPrice());
            cartstorage.addItem(item);
            quantityField.clear();
        } catch (NumberFormatException e) {
            System.out.println("Input kuantitas tidak valid.");
        }
    }

    private void handleBack(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close(); // karena tadi dipanggil pakai new Stage(), tinggal ditutup saja
    }
}
