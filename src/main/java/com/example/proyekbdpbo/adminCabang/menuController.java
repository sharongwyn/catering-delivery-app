package com.example.proyekbdpbo.adminCabang;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.utils.SessionCabang;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class menuController implements Initializable {
    @FXML private ChoiceBox<Menu> menu1ChoiceBox;
    @FXML private ChoiceBox<Menu> menu2ChoiceBox;
    @FXML private ChoiceBox<Menu> menu3ChoiceBox;
    @FXML private ChoiceBox<Menu> menu4ChoiceBox;
    @FXML private ChoiceBox<Menu> menu5ChoiceBox;
    @FXML private ChoiceBox<Menu> menu6ChoiceBox;
    @FXML private DatePicker datePicker;
    @FXML private Button confirm;

    @FXML
    private ImageView menuIcon;

    @FXML
    private ImageView verifIcon;

    @FXML
    private ImageView deliveryIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DatabaseConnection.getConnection();
        loadMenuChoices();

        confirm.setOnAction(e -> confirmButton());
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

    private void loadMenuChoices() {
        List<Menu> menus = new ArrayList<>();

        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_menuharian, nama_menu FROM menu_harian");

            while (rs.next()) {
                int id = rs.getInt("id_menuharian");
                String nama = rs.getString("nama_menu");
                menus.add(new Menu(id, nama));
            }

            for (ChoiceBox<Menu> cb : List.of(menu1ChoiceBox, menu2ChoiceBox, menu3ChoiceBox, menu4ChoiceBox, menu5ChoiceBox, menu6ChoiceBox)) {
                cb.getItems().addAll(menus);
            }

        } catch (SQLException e) {
            showAlert("Gagal load menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void confirmButton() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert("Choose a date first!", Alert.AlertType.WARNING);
            return;
        }

        List<ChoiceBox<Menu>> boxes = List.of(menu1ChoiceBox, menu2ChoiceBox, menu3ChoiceBox, menu4ChoiceBox, menu5ChoiceBox, menu6ChoiceBox);

        try {
            int idCabang = SessionCabang.getIdCabang();
            String sql = "INSERT INTO menu_harian_cabang (id_cabang, tanggal_menu, id_menuharian) VALUES (?, ?, ?)";
            PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql);

            int count = 0;

            for (ChoiceBox<Menu> cb : boxes) {
                Menu selected = cb.getValue();
                if (selected != null) {
                    pstmt.setInt(1, idCabang);
                    pstmt.setDate(2, Date.valueOf(selectedDate));
                    pstmt.setInt(3, selected.getId());
                    pstmt.addBatch();
                    count++;
                }
            }

            if (count > 0) {
                pstmt.executeBatch();
                showAlert("Berhasil menyimpan " + count + " menu!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Tidak ada menu yang dipilih.", Alert.AlertType.WARNING);
            }

        } catch (SQLException e) {
            showAlert("Gagal insert menu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Kelas Menu untuk ditampilkan di ChoiceBox
    public static class Menu {
        private final int id;
        private final String nama;

        public Menu(int id, String nama) {
            this.id = id;
            this.nama = nama;
        }

        public int getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        @Override
        public String toString() {
            return nama;
        }
    }
}
