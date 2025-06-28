package com.example.proyekbdpbo.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;


import java.io.IOException;

public class testController {
    @FXML
    private Button tesBtn;
    @FXML
    private void handleTestButton() {
        int testOrderId = 1;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/router-view.fxml"));
            Parent root = loader.load();

            routerController router = loader.getController();
            router.setOrderId(testOrderId); // userId bebas atau 0

            Stage stage = (Stage) tesBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Keranjang Saya");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
