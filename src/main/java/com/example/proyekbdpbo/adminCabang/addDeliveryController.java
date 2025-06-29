package com.example.proyekbdpbo.adminCabang;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.utils.SessionCabang;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class addDeliveryController {
    @FXML
    private DatePicker deliveryDatePicker;
    @FXML
    private ChoiceBox<String> staffChoiceBox;
    @FXML
    private ChoiceBox<String> plateChoiceBox;
    @FXML
    private TextField deliveryTimeField;
    @FXML
    private TextField estimatedArrivalField;
    @FXML
    private Button addDeliveryButton;
    @FXML
    private Button backBtn;

    @FXML
    public void initialize() {
        loadStaffPengiriman();
        loadKendaraan();
        addDeliveryButton.setCursor(Cursor.HAND);
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnMouseClicked(e -> goBack());
    }



    private void loadStaffPengiriman() {
        int idCabang = SessionCabang.getIdCabang();

        String query = "SELECT nama_staffPengiriman FROM STAFF_PENGIRIMAN WHERE id_cabang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idCabang);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                staffChoiceBox.getItems().add(rs.getString("nama_staffPengiriman"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadKendaraan() {
        int idCabang = SessionCabang.getIdCabang();

        String query = "SELECT plat_nomor FROM KENDARAAN WHERE id_cabang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idCabang);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                plateChoiceBox.getItems().add(rs.getString("plat_nomor"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddDelivery() {
        String staffName = staffChoiceBox.getValue();
        String plateNumber = plateChoiceBox.getValue();
        LocalDate deliveryDate = deliveryDatePicker.getValue();
        String deliveryTime = deliveryTimeField.getText();
        String estimatedArrival = estimatedArrivalField.getText(); // e.g. 13:00

        if (staffName == null || plateNumber == null || deliveryDate == null ||
                deliveryTime.isEmpty() || estimatedArrival.isEmpty()) {
            System.err.println("Semua field wajib diisi.");
            return;
        }

        // Validasi: tanggal tidak boleh sebelum hari ini
        if (deliveryDate.isBefore(LocalDate.now())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Date");
            alert.setHeaderText(null);
            alert.setContentText("The delivery date cannot be before today.");
            alert.showAndWait();
            return;
        }

        try {
            LocalTime.parse(deliveryTime);
            LocalTime.parse(estimatedArrival);
        } catch (DateTimeParseException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Time Format");
            alert.setHeaderText(null);
            alert.setContentText("Format waktu tidak valid. Gunakan format HH:mm (contoh: 09:30 atau 14:00)");
            alert.showAndWait();
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ambil id_staffPengiriman
            int staffId = -1;
            PreparedStatement staffStmt = conn.prepareStatement("SELECT id_staffPengiriman FROM STAFF_PENGIRIMAN WHERE nama_staffPengiriman = ?");
            staffStmt.setString(1, staffName);
            ResultSet rs1 = staffStmt.executeQuery();
            if (rs1.next()) {
                staffId = rs1.getInt("id_staffPengiriman");
            }
            staffStmt.close();

            // Ambil id_kendaraan
            int kendaraanId = -1;
            PreparedStatement platStmt = conn.prepareStatement("SELECT id_kendaraan FROM KENDARAAN WHERE plat_nomor = ?");
            platStmt.setString(1, plateNumber);
            ResultSet rs2 = platStmt.executeQuery();
            if (rs2.next()) {
                kendaraanId = rs2.getInt("id_kendaraan");
            }
            platStmt.close();

            if (staffId == -1 || kendaraanId == -1) {
                System.err.println("Staff or Vehicle data not found. ");
                return;
            }

            // Insert ke PENGIRIMAN
            PreparedStatement insertStmt = conn.prepareStatement("""
            INSERT INTO PENGIRIMAN (id_staffPengiriman, id_kendaraan, tanggal_pengiriman, jam_pengiriman, estimasi_sampai,id_cabang)
            VALUES (?, ?, ?, ?, ?,?)
        """);
            insertStmt.setInt(1, staffId);
            insertStmt.setInt(2, kendaraanId);
            insertStmt.setDate(3, java.sql.Date.valueOf(deliveryDate));
            insertStmt.setTime(4, java.sql.Time.valueOf(deliveryTime + ":00"));
            insertStmt.setTime(5, java.sql.Time.valueOf(estimatedArrival + ":00"));
            insertStmt.setInt(6, SessionCabang.getIdCabang());

            int rows = insertStmt.executeUpdate();
            if (rows > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("A new delivery has been scheduled!");
                alert.showAndWait();
            }

            insertStmt.close();
            goBack();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/adminc-delivery-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
