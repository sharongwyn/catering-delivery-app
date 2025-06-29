package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.utils.Session;
import com.example.proyekbdpbo.utils.SessionCabang;
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

public class loginController {
    @FXML private TextField usnField;
    @FXML private PasswordField passField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button loginButton;
    @FXML private Button registButton;

    public void initialize() {
        roleComboBox.getItems().addAll("pelanggan", "admin cabang", "admin pusat");
        loginButton.setCursor(Cursor.HAND);
        registButton.setCursor(Cursor.HAND);
    }

    public void loginAction() {
        String username = usnField.getText();
        String password = passField.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Semua field harus diisi.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?::role_enum";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int idUser = rs.getInt("id_user");

                // Simpan ke session
                Session.setUser(idUser, username);

                // Jika admin cabang → ambil id_cabang
                if (role.equalsIgnoreCase("admin cabang")) {
                    String cabangQuery = "SELECT id_cabang FROM ADMIN_CABANG WHERE id_user = ?";
                    PreparedStatement cabangPs = conn.prepareStatement(cabangQuery);
                    cabangPs.setInt(1, idUser);
                    ResultSet cabangRs = cabangPs.executeQuery();

                    if (cabangRs.next()) {
                        int idCabang = cabangRs.getInt("id_cabang");
                        SessionCabang.setIdCabang(idCabang);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Gagal", "Admin cabang tidak memiliki cabang.");
                        return;
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Login Berhasil", "Hello, " + username);
                bukaHalamanSesuaiRole(role, idUser, username);

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username atau password salah");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bukaHalamanSesuaiRole(String role, int idUser, String username) {
        try {
            FXMLLoader loader;
            Parent root;

            switch (role.toLowerCase()) {
                case "pelanggan":
                    loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-home-view.fxml"));
                    root = loader.load();

                    break;

                case "admin cabang":
                    loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/adminc-menu-view.fxml"));
                    root = loader.load();
                    break;

                case "admin pusat":
                    loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/adminp-performance-view.fxml"));
                    root = loader.load();
                    break;

                default:
                    showAlert(Alert.AlertType.WARNING, "Peran Tidak Dikenali", "Role tidak didukung.");
                    return;
            }

            // Tampilkan halaman baru di scene yang sama
            Stage stage = (Stage) usnField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(role + " page");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Halaman", "Tidak dapat memuat halaman untuk " + role);
        }
    }

    public void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void regButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-regist-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) usnField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Registrasi");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Pindah Halaman", "Tidak dapat memuat halaman registrasi.");
        }
    }
}
