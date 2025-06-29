package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

public class status3Controller {
    private int orderId;
    @FXML
    private Label platNomorLabel;

    @FXML
    private Label jenisKendaraanLabel;

    @FXML
    private Label namaStaffLabel;

    @FXML
    private Label estimationTime;

    @FXML
    private Button backBtn;

    public void setOrderId(int orderId) {
        this.orderId = orderId;
        loadDeliveryDetails();

        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnMouseClicked(e -> goBack());
    }


    private void loadDeliveryDetails() {
        String query = """
        SELECT 
            k.plat_nomor,
            k.jenis_kendaraan,
            s.nama_staffPengiriman,
            p.estimasi_sampai
        FROM "ORDER" o
        JOIN pengiriman p ON o.id_pengiriman = p.id_pengiriman
        JOIN kendaraan k ON p.id_kendaraan = k.id_kendaraan
        JOIN staff_pengiriman s ON p.id_staffPengiriman = s.id_staffPengiriman
        WHERE o.id_order = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                platNomorLabel.setText(rs.getString("plat_nomor"));
                jenisKendaraanLabel.setText(rs.getString("jenis_kendaraan"));
                namaStaffLabel.setText(rs.getString("nama_staffPengiriman"));
                estimationTime.setText(rs.getString("estimasi_sampai"));
            } else {
                platNomorLabel.setText("N/A");
                jenisKendaraanLabel.setText("N/A");
                namaStaffLabel.setText("N/A");
                estimationTime.setText("N/A");
            }

        } catch (SQLException e) {
            platNomorLabel.setText("Error");
            jenisKendaraanLabel.setText("Error");
            namaStaffLabel.setText("Error");
            estimationTime.setText("Error");
            e.printStackTrace();
        }
    }
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-history-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
