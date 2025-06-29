package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class profileController {
    @FXML
    private ImageView homeIcon, orderIcon, historyIcon, profileIcon;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label usnLabel, namaLabel, alamatLabel,
            telpLabel, idMemberLabel, tanggalLabel, poinLabel;
    @FXML
    private Button statusLabel;

    private String currentUsername;
    private String fotoPath;
    private String usn;

    public void setUsername(String username) {
        this.currentUsername = username;
        loadUserData();
    }


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

    public void loadUserData() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT p.nama_pelanggan, p.alamat_pelanggan, p.no_telp, m.id_member, m.tanggal_gabung, m.poin " +
                            "FROM pelanggan p LEFT JOIN member m ON p.id_pelanggan = m.id_pelanggan " +
                            "WHERE p.id_user = ?"
            );
            stmt.setInt(1, Session.getIdPelanggan());


            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                namaLabel.setText(rs.getString("nama_pelanggan"));
                alamatLabel.setText(rs.getString("alamat_pelanggan"));
                telpLabel.setText(rs.getString("no_telp"));

                if (rs.getString("id_member") != null) {
                    idMemberLabel.setText(rs.getString("id_member"));
                    tanggalLabel.setText(rs.getString("tanggal_gabung"));
                    poinLabel.setText(String.valueOf(rs.getInt("poin")));
                } else {
                    idMemberLabel.setText("Belum Terdaftar");
                    tanggalLabel.setText("Belum Terdaftar");
                    poinLabel.setText("Belum Terdaftar");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showJoinMemberAlert(String username) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO member (username, tanggal_gabung, point_member) VALUES (?, CURRENT_DATE, 0)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        try (conn){
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);  // ambil dari parameter
            stmt.executeUpdate();

            System.out.println("Berhasil join member.");

        } catch (SQLException e) {
            e.printStackTrace();
            // Bisa tambahkan alert gagal juga
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Gagal");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Gagal join member!");
            errorAlert.showAndWait();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void joinAsMember(String username) {
        String insertSQL = "INSERT INTO member (id_member, tanggal_gabung, point_member) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            long memberId = generateMemberId();
            stmt.setString(1, username);
            stmt.setInt(1, (int) memberId);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            stmt.setInt(3, 0);
            stmt.executeUpdate();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Sukses");
            success.setHeaderText("Sukses menjadi member!");
            success.setContentText("Selamat, kamu sekarang sudah menjadi member.");
            success.show();

            loadUserData(); // reload tampilan

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private long generateMemberId() {
        return System.currentTimeMillis();
    }

    @FXML
    public void joinMember(javafx.event.ActionEvent event) throws SQLException {
        profileController controller = new profileController();
        controller.showJoinMemberAlert(usn);
    }

    @FXML
    public void logOutButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-login-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
