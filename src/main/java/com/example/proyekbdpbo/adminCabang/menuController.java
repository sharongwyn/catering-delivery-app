package com.example.proyekbdpbo.adminCabang;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class menuController {
    @FXML
    private ImageView menuIcon;

    @FXML
    private ImageView verifIcon;

    @FXML
    private ImageView deliveryIcon;

    @FXML
    private void initialize(){
        menuIcon.setCursor(Cursor.HAND);
        verifIcon.setCursor(Cursor.HAND);
        deliveryIcon.setCursor(Cursor.HAND);

        menuIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/adminc-menu-view.fxml"));
        verifIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/adminc-veriforder-view.fxml"));
        deliveryIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/adminc-delivery-view.fxml"));
    }
    private void switchScene(String fxmlPath) {
        try {
            Stage stage = (Stage) menuIcon.getScene().getWindow(); // ambil window dari salah satu ikon
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
