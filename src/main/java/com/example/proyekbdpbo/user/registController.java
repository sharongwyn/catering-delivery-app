package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class registController {
    @FXML
    private TextField namaField;

    @FXML
    private TextField userField;

    @FXML
    private TextField telpField;

    @FXML
    private TextField alamatField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registButton;

    @FXML
    private ChoiceBox<String> kotaField;

    @FXML
    private void initialize(){
        registButton.setCursor(Cursor.HAND);

        kotaField.getItems().addAll("Surabaya", "Jakarta", "Bandung", "Yogyakarta", "Semarang");
        kotaField.setValue("Surabaya");
    }

    @FXML
    public void handleRegister() {
        String nama = namaField.getText();
        String username = userField.getText();
        String telp = telpField.getText();
        String alamat = alamatField.getText();
        String password = passwordField.getText();
        String kota = kotaField.getValue();

        if (nama.isEmpty() || username.isEmpty() || telp.isEmpty() || alamat.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Semua field harus diisi!");
            return;
        }

        DatabaseConnection dbConn = new DatabaseConnection();
        try (Connection conn = dbConn.getConnection()) {
            // 1. Insert ke users dan ambil generated key (id_user)
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, 'pelanggan') RETURNING id_user";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id_user = rs.getInt("id_user");

                // 2. Insert ke pelanggan pakai id_user
                String query2 = "INSERT INTO pelanggan (id_user, nama_pelanggan, alamat_pelanggan, no_telp, kota) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt2 = conn.prepareStatement(query2);
                stmt2.setInt(1, id_user);
                stmt2.setString(2, nama);
                stmt2.setString(3, alamat);
                stmt2.setString(4, telp);
                stmt2.setObject(5, kota, java.sql.Types.OTHER);

                int result = stmt2.executeUpdate();
                if (result > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Registered successfully!");
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-login-view.fxml"));
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) userField.getScene().getWindow(); // ambil window saat ini
                        stage.setScene(scene);
                        stage.setTitle("Login");
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Fail to load page", "Fail to load login page.");
                    }
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal insert user.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error Database", e.getMessage());
        }
    }

    public void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
