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
import javafx.util.Callback;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class updateDeliveryController {
    @FXML private DatePicker deliveryDatePicker;
    @FXML private ChoiceBox<String> staffChoiceBox;
    @FXML private ChoiceBox<String> plateChoiceBox;
    @FXML private TextField deliveryTimeField;
    @FXML private TextField estimatedArrivalField;
    @FXML private Button updateDeliveryButton;
    @FXML private Button backBtn;

    private int deliveryId;

    public void setDeliveryId(int deliveryId) {
        this.deliveryId = deliveryId;
        loadCurrentDeliveryData();
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnMouseClicked(e -> goBack());
    }

    @FXML
    public void initialize() {
        loadStaffOptions();
        loadKendaraanOptions();

        updateDeliveryButton.setOnAction(e -> updateDelivery());
    }

    private void loadStaffOptions() {
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

    private void loadKendaraanOptions() {
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

    private void loadCurrentDeliveryData() {
        String sql = """
            SELECT 
                d.tanggal_pengiriman, d.jam_pengiriman, d.estimasi_sampai,
                s.nama_staffPengiriman, k.plat_nomor
            FROM PENGIRIMAN d
            JOIN STAFF_PENGIRIMAN s ON d.id_staffPengiriman = s.id_staffPengiriman
            JOIN KENDARAAN k ON d.id_kendaraan = k.id_kendaraan
            WHERE d.id_pengiriman = ? AND d.id_cabang = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deliveryId);
            stmt.setInt(2, SessionCabang.getIdCabang());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                deliveryDatePicker.setValue(rs.getDate("tanggal_pengiriman").toLocalDate());
                deliveryTimeField.setText(rs.getTime("jam_pengiriman").toString().substring(0,5));
                estimatedArrivalField.setText(rs.getTime("estimasi_sampai").toString().substring(0,5));
                staffChoiceBox.setValue(rs.getString("nama_staffPengiriman"));
                plateChoiceBox.setValue(rs.getString("plat_nomor"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDelivery() {
        String staffName = staffChoiceBox.getValue();
        String plateNumber = plateChoiceBox.getValue();
        LocalDate deliveryDate = deliveryDatePicker.getValue();
        String deliveryTime = deliveryTimeField.getText();
        String estimatedArrival = estimatedArrivalField.getText();

        if (staffName == null || plateNumber == null || deliveryDate == null ||
                deliveryTime.isEmpty() || estimatedArrival.isEmpty()) {
            showAlert(AlertType.ERROR, "Form not complete", "All fields must be filled!");
            return;
        }

        // Validasi: tanggal tidak boleh mundur
        if (deliveryDate.isBefore(LocalDate.now())) {
            showAlert(AlertType.WARNING, "Invalid Date", "The delivery date cannot be before today.");
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
            // Ambil ID Staff dan Kendaraan berdasarkan nama & plat
            int staffId = getIdFromName(conn, "STAFF_PENGIRIMAN", "nama_staffPengiriman", staffName);
            int kendaraanId = getIdFromName(conn, "KENDARAAN", "plat_nomor", plateNumber);

            if (staffId == -1 || kendaraanId == -1) {
                showAlert(AlertType.ERROR, "Data not found", "Staff or vehicle not found");
                return;
            }

            String sql = """
                UPDATE PENGIRIMAN
                SET id_staffPengiriman = ?, id_kendaraan = ?, tanggal_pengiriman = ?, 
                    jam_pengiriman = ?, estimasi_sampai = ?
                WHERE id_pengiriman = ? AND id_cabang = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, staffId);
            stmt.setInt(2, kendaraanId);
            stmt.setDate(3, java.sql.Date.valueOf(deliveryDate));
            stmt.setTime(4, java.sql.Time.valueOf(deliveryTime + ":00"));
            stmt.setTime(5, java.sql.Time.valueOf(estimatedArrival + ":00"));
            stmt.setInt(6, deliveryId);
            stmt.setInt(7, SessionCabang.getIdCabang());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert(AlertType.INFORMATION, "Success", "Delivery has been updated.");
            } else {
                showAlert(AlertType.WARNING, "No Change", "No changes were made.");
            }

            stmt.close();
            goBack();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "SQL Error", e.getMessage());
        }
    }

    private int getIdFromName(Connection conn, String table, String field, String value) throws SQLException {
        String idColumn;
        if (table.equals("STAFF_PENGIRIMAN")) {
            idColumn = "id_staffPengiriman";
        } else if (table.equals("KENDARAAN")) {
            idColumn = "id_kendaraan";
        } else {
            throw new IllegalArgumentException("Unsupported table: " + table);
        }

        String sql = "SELECT " + idColumn + " FROM " + table + " WHERE " + field + " = ? AND id_cabang = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setInt(2, SessionCabang.getIdCabang());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
