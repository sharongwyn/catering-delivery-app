package com.example.proyekbdpbo.adminCabang;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.utils.SessionCabang;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
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
            showAlert("Please choose a date first!", Alert.AlertType.WARNING);
            return;
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            showAlert("Date cannot be before today!", Alert.AlertType.WARNING);
            return;
        }

        int idCabang = SessionCabang.getIdCabang();
        List<ChoiceBox<Menu>> boxes = List.of(menu1ChoiceBox, menu2ChoiceBox, menu3ChoiceBox, menu4ChoiceBox, menu5ChoiceBox, menu6ChoiceBox);

        // Ambil menu yang dipilih user
        List<Menu> selectedMenus = new ArrayList<>();
        for (ChoiceBox<Menu> cb : boxes) {
            Menu selected = cb.getValue();
            if (selected != null) {
                selectedMenus.add(selected);
            }
        }

        if (selectedMenus.isEmpty()) {
            showAlert("Minimum one menu should be selected!", Alert.AlertType.WARNING);
            return;
        }

        // Cek duplikat dalam input
        long distinctCount = selectedMenus.stream().map(Menu::getId).distinct().count();
        if (distinctCount < selectedMenus.size()) {
            showAlert("There should be no same menus on one date!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Hitung menu yang sudah ada di tanggal tsb untuk cabang tsb
            String countQuery = "SELECT COUNT(*) FROM menu_harian_cabang WHERE id_cabang = ? AND tanggal_menu = ?";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            countStmt.setInt(1, idCabang);
            countStmt.setDate(2, Date.valueOf(selectedDate));
            ResultSet rsCount = countStmt.executeQuery();

            int existingCount = 0;
            if (rsCount.next()) {
                existingCount = rsCount.getInt(1);
            }

            if (existingCount >= 6) {
                showAlert("Menu on that date is already full (max. 6).", Alert.AlertType.WARNING);
                return;
            }

            int remainingSlots = 6 - existingCount;
            if (selectedMenus.size() > remainingSlots) {
                showAlert("You can only add " + remainingSlots + " more menus for this date", Alert.AlertType.WARNING);
                return;
            }

            // Cek apakah menu yang dipilih sudah ada sebelumnya (di tanggal & cabang yang sama)
            String checkQuery = "SELECT id_menuharian FROM menu_harian_cabang WHERE id_cabang = ? AND tanggal_menu = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, idCabang);
            checkStmt.setDate(2, Date.valueOf(selectedDate));
            ResultSet rsCheck = checkStmt.executeQuery();

            List<Integer> existingMenuIds = new ArrayList<>();
            while (rsCheck.next()) {
                existingMenuIds.add(rsCheck.getInt("id_menuharian"));
            }

            for (Menu menu : selectedMenus) {
                if (existingMenuIds.contains(menu.getId())) {
                    showAlert("Menu \"" + menu.getNama() + "\" already exists in that date!", Alert.AlertType.WARNING);
                    return;
                }
            }

            // Insert menu ke tabel
            String insertQuery = "INSERT INTO menu_harian_cabang (id_cabang, tanggal_menu, id_menuharian) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

            for (Menu menu : selectedMenus) {
                insertStmt.setInt(1, idCabang);
                insertStmt.setDate(2, Date.valueOf(selectedDate));
                insertStmt.setInt(3, menu.getId());
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            showAlert("Successfully saved " + selectedMenus.size() + " menu!", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Fail to insert menu: " + e.getMessage(), Alert.AlertType.ERROR);
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

    @FXML
    private void logOutButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
