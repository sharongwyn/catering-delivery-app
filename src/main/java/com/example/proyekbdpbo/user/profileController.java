package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class profileController {
    private int idPelanggan;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label usnLabel, namaLabel, alamatLabel,
            telpLabel, idMemberLabel, tanggalLabel, poinLabel;
    @FXML
    private Button statusLabel;
    @FXML
    private ImageView homeIcon, orderIcon, historyIcon, profileIcon;

    private String currentUsername;
    private String usn;
    private int currentUserId;

    private int userId;

    public void setUsername(String username) {
        this.currentUsername = username;
    }

    public void setUserInfo(int idUser, String username) {
        this.currentUserId = idUser;
        this.currentUsername = username;
        loadUserData();
    }

    @FXML
    private void initialize(){
        homeIcon.setCursor(Cursor.HAND);
        orderIcon.setCursor(Cursor.HAND);
        historyIcon.setCursor(Cursor.HAND);
        profileIcon.setCursor(Cursor.HAND);

        homeIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-home-view.fxml"));
        orderIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-order-view.fxml"));
        historyIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-history-view.fxml"));
        profileIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-profile-view.fxml"));

        loadUserData();
    }

    private void switchScene(String fxmlPath) {
        try {
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadUserData() {
        int userId = Session.getIdPelanggan();
        String username = Session.getNamaPelanggan();

        if (userId == 0 || username == null) {
            System.out.println("Session kosong.");
            return;
        }

        this.currentUserId = userId;
        this.currentUsername = username;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT p.id_pelanggan, p.nama_pelanggan, p.alamat_pelanggan, p.no_telp, " +
                    "m.id_member, m.tanggal_gabung, m.point_member " +
                    "FROM pelanggan p " +
                    "LEFT JOIN member m ON p.id_pelanggan = m.id_pelanggan " +
                    "WHERE p.id_user = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                usnLabel.setText(username);
                namaLabel.setText(rs.getString("nama_pelanggan"));
                alamatLabel.setText(rs.getString("alamat_pelanggan"));
                telpLabel.setText(rs.getString("no_telp"));
                this.idPelanggan = rs.getInt("id_pelanggan");

                if (rs.getString("id_member") != null) {
                    idMemberLabel.setText(rs.getString("id_member"));
                    tanggalLabel.setText(rs.getString("tanggal_gabung"));
                    poinLabel.setText(String.valueOf(rs.getInt("point_member")));
                    statusLabel.setText("Sudah Member");
                    statusLabel.setDisable(true);
                } else {
                    idMemberLabel.setText("Belum Terdaftar");
                    tanggalLabel.setText("Belum Terdaftar");
                    poinLabel.setText("Belum Terdaftar");
                    statusLabel.setText("Join Member");
                    statusLabel.setDisable(false);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void showJoinMemberAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Join Member");
        alert.setHeaderText(null);
        alert.setContentText("Mau jadi member?");

        // Kustom tombol YES dan BACK
        ButtonType yesButton = new ButtonType("YES", ButtonBar.ButtonData.OK_DONE);
        ButtonType backButton = new ButtonType("BACK", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, backButton);

        // Tampilkan alert dan tangkap hasilnya
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // Jika YES → tampilkan alert sukses
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Berhasil");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Success!");
            successAlert.showAndWait();

            loadUserData();
        }
        // Jika BACK, tidak melakukan apa-apa
    }


//    public void showJoinMemberAlert(String username) throws SQLException {
//        Connection conn = DatabaseConnection.getConnection();
//        String sql = "INSERT INTO member (id_member, tanggal_gabung, point_member) VALUES (?, CURRENT_DATE, 0)";
//        PreparedStatement stmt = conn.prepareStatement(sql);
//        try (conn){
//            stmt = conn.prepareStatement(sql);
//            stmt.setString(1, username);  // ambil dari parameter
//            stmt.executeUpdate();
//
//            System.out.println("Berhasil join member.");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            // Bisa tambahkan alert gagal juga
//            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//            errorAlert.setTitle("Gagal");
//            errorAlert.setHeaderText(null);
//            errorAlert.setContentText("Gagal join member!");
//            errorAlert.showAndWait();
//        } finally {
//            try {
//                if (stmt != null) stmt.close();
//                if (conn != null) conn.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//
//    private void joinAsMember(String username) {
//        String insertSQL = "INSERT INTO member (id_member, tanggal_gabung, point_member) VALUES (?, ?, ?)";
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
//
//            long memberId = generateMemberId();
//            stmt.setString(1, username);
//            stmt.setInt(1, (int) memberId);
//            stmt.setDate(2, Date.valueOf(LocalDate.now()));
//            stmt.setInt(3, 0);
//            stmt.executeUpdate();
//
//            Alert success = new Alert(Alert.AlertType.INFORMATION);
//            success.setTitle("Sukses");
//            success.setHeaderText("Sukses menjadi member!");
//            success.setContentText("Selamat, kamu sekarang sudah menjadi member.");
//            success.show();
//
//            loadUserData(); // reload tampilan
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public void joinAsMember() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Join Member");
        alert.setHeaderText(null);
        alert.setContentText("Mau jadi member?");

        ButtonType yesButton = new ButtonType("YES", ButtonBar.ButtonData.OK_DONE);
        ButtonType backButton = new ButtonType("BACK", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, backButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Cek apakah sudah jadi member
                String checkQuery = "SELECT id_member FROM member WHERE id_pelanggan = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, idPelanggan);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    showAlert(Alert.AlertType.INFORMATION, "Info", "Kamu sudah jadi member.");
                    return;
                }

                // Cari id_member terakhir
                String getMaxIdQuery = "SELECT COALESCE(MAX(id_member), 0) + 1 AS next_id FROM member";
                PreparedStatement maxStmt = conn.prepareStatement(getMaxIdQuery);
                ResultSet rsMax = maxStmt.executeQuery();
                int nextId = 1;
                if (rsMax.next()) {
                    nextId = rsMax.getInt("next_id");
                }

                // Insert data member baru
                String insertQuery = "INSERT INTO member (id_member, id_pelanggan, tanggal_gabung, point_member, status_member) " +
                        "VALUES (?, ?, CURRENT_DATE, 0, 'aktif')";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, nextId);
                insertStmt.setInt(2, idPelanggan);
                insertStmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Selamat, kamu telah menjadi member!");
                loadUserData(); // update UI

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal mendaftar member.");
            }
        }
    }


    private long generateMemberId() {
        return System.currentTimeMillis();
    }

    @FXML
    public void joinMember(ActionEvent event) throws SQLException {
        joinAsMember();
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

    public void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}


