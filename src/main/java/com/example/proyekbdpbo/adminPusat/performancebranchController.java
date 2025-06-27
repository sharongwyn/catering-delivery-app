package com.example.proyekbdpbo.adminPusat;

import com.example.proyekbdpbo.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class performancebranchController {
    @FXML private Label branchLabel;
    @FXML private Label ratingLabel;
    @FXML private Label addressbranch;
    @FXML private Label telpbranch;

    @FXML private Label r1idpelanggan;
    @FXML private Label r1review;
    @FXML private Label r2idpelanggan;
    @FXML private Label r2review;
    @FXML private Label r3idpelanggan;
    @FXML private Label r3review;

    @FXML private Button backButton;

    private int selectedBranchId; // akan diisi dari controller sebelumnya

    public void setBranchId(int branchId) {
        this.selectedBranchId = branchId;
        loadBranchDetails();
        loadTopReviews();
    }

    @FXML
    public void initialize() {
        backButton.setOnAction(e -> handleBack());
    }

    private void loadBranchDetails() {
        String sql = "SELECT nama_cabang, average_rating, alamat_cabang, telp_cabang FROM cabang WHERE id_cabang = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedBranchId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                branchLabel.setText(rs.getString("nama_cabang"));
                ratingLabel.setText(String.format("%.1f", rs.getDouble("average_rating")));
                addressbranch.setText(rs.getString("alamat_cabang"));
                telpbranch.setText(rs.getString("telp_cabang"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTopReviews() {
        String sql = "SELECT id_pelanggan, ulasan_cabang FROM rating_cabang WHERE id_cabang = ? ORDER BY id_rating DESC LIMIT 3";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedBranchId);
            ResultSet rs = stmt.executeQuery();

            Label[] usernames = { r1idpelanggan, r2idpelanggan, r3idpelanggan };
            Label[] reviews = { r1review, r2review, r3review };

            int i = 0;
            while (rs.next() && i < 3) {
                String userId = "ID: " + rs.getInt("id_pelanggan");
                String review = rs.getString("ulasan_cabang");
                usernames[i].setText(userId);
                reviews[i].setText(review);
                i++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/adminPusat/adminp-performance-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
