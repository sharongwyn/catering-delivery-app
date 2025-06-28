package com.example.proyekbdpbo.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class homeController {
    @FXML
    private ImageView homeIcon, orderIcon, historyIcon, profileIcon;

    @FXML
    public void initialize() {
        homeIcon.setCursor(Cursor.HAND);
        orderIcon.setCursor(Cursor.HAND);
        historyIcon.setCursor(Cursor.HAND);
        profileIcon.setCursor(Cursor.HAND);

        homeIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-home-view.fxml"));
        orderIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-order-view.fxml"));
        historyIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-history-view.fxml"));
        profileIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-profile-view.fxml"));
    }

    private void switchScene(String fxmlPath) {
        try {
            Stage stage = (Stage) homeIcon.getScene().getWindow(); // ambil window dari salah satu ikon
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
