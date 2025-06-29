package com.example.proyekbdpbo.adminCabang;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.Delivery;
import com.example.proyekbdpbo.utils.SessionCabang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class deliveryController {
    @FXML
    private TableView<Delivery> deliveryTable;

    @FXML
    private Button addDeliveryButton;

    @FXML
    private Button updateDeliveryButton;

    @FXML
    private Button deleteDeliveryButton;

    @FXML
    private DatePicker filterDatePicker;

    @FXML
    private ImageView menuIcon;

    @FXML
    private ImageView verifIcon;

    @FXML
    private ImageView deliveryIcon;

    @FXML
    private TableColumn<Delivery, Number> colId;
    @FXML
    private TableColumn<Delivery, String> colStaffName, colJenis, colPlat, colJam, colEstimasi;

    private ObservableList<Delivery> allDeliveries= FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // set cell value factory
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colStaffName.setCellValueFactory(data -> data.getValue().staffNameProperty());
        colJenis.setCellValueFactory(data -> data.getValue().jenisKendaraanProperty());
        colPlat.setCellValueFactory(data -> data.getValue().platNomorProperty());
        colJam.setCellValueFactory(data -> data.getValue().jamProperty());
        colEstimasi.setCellValueFactory(data -> data.getValue().estimasiProperty());

        addDeliveryButton.setCursor(Cursor.HAND);
        updateDeliveryButton.setCursor(Cursor.HAND);
        deleteDeliveryButton.setCursor(Cursor.HAND);


        loadDeliveryData();
        filterDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            filterByDate(newDate);
        });

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

    private void filterByDate(LocalDate date) {
        if (date == null) {
            deliveryTable.setItems(allDeliveries);
            return;
        }

        ObservableList<Delivery> filtered = FXCollections.observableArrayList();
        for (Delivery d : allDeliveries) {
            if (d.getTanggalPengiriman().equals(date)) {
                filtered.add(d);
            }
        }
        deliveryTable.setItems(filtered);
    }

    private void loadDeliveryData() {
        allDeliveries.clear();

        String query = """
        SELECT p.id_pengiriman, s.nama_staffPengiriman, k.jenis_kendaraan, k.plat_nomor,
               p.jam_pengiriman, p.estimasi_sampai, p.tanggal_pengiriman
        FROM PENGIRIMAN p
        JOIN STAFF_PENGIRIMAN s ON p.id_staffPengiriman = s.id_staffPengiriman
        JOIN KENDARAAN k ON p.id_kendaraan = k.id_kendaraan
        WHERE p.id_cabang = ? -- filter berdasarkan cabang login, jika pakai session
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, SessionCabang.getIdCabang());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Delivery d = new Delivery(
                        rs.getInt("id_pengiriman"),
                        rs.getString("nama_staffPengiriman"),
                        rs.getString("jenis_kendaraan"),
                        rs.getString("plat_nomor"),
                        rs.getString("jam_pengiriman"),
                        rs.getString("estimasi_sampai"),
                        rs.getDate("tanggal_pengiriman").toLocalDate()
                );
                allDeliveries.add(d);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        deliveryTable.setItems(allDeliveries);
    }

    @FXML
    private void handleAddDelivery() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/adminc-addDelivery-view.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) deliveryTable.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleUpdateDelivery() throws IOException {
        Delivery selected = deliveryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a delivery to update.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/adminc-updateDelivery-view.fxml"));
        Parent root = loader.load();

        updateDeliveryController controller = loader.getController();
        controller.setDeliveryId(selected.getId());

        Stage stage = (Stage) deliveryTable.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleDeleteDelivery() {
        Delivery selected = deliveryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a delivery to delete.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM PENGIRIMAN WHERE id_pengiriman = ?");
            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();
            allDeliveries.remove(selected);
            showAlert("Delivery deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
