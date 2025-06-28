package com.example.proyekbdpbo.adminCabang;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.Delivery;
import com.example.proyekbdpbo.model.orderData;
import com.example.proyekbdpbo.utils.SessionCabang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class verifOrderController {
    @FXML
    private ImageView menuIcon;

    @FXML
    private ImageView verifIcon;

    @FXML
    private ImageView deliveryIcon;

    @FXML private TableView<orderData> orderTable;
    @FXML private TableColumn<orderData, Integer> orderIdCol;
    @FXML private TableColumn<orderData, String> nameCol;
    @FXML private TableColumn<orderData, Double> priceCol;
    @FXML private TableColumn<orderData, String> scheduleCol;
    @FXML private TableColumn<orderData, Integer> deliveryCol;
    @FXML private TableColumn<orderData, String> statusCol;

    @FXML private ChoiceBox<Integer> deliveryDropdown;
    @FXML private ChoiceBox<String> statusDropdown;
    @FXML private Button updateButton;
    @FXML private Button addDeliveryButton;
    @FXML private DatePicker filterDatePicker;


    private ObservableList<orderData> orderList = FXCollections.observableArrayList();

    @FXML
    private void initialize(){
        SessionCabang.setIdCabang(1); // Ganti sesuai session login

        setupColumns();
        loadOrders();
        loadDeliveryOptions();
        loadStatusOptions();

        updateButton.setOnAction(e -> handleUpdateStatus());
        addDeliveryButton.setOnAction(e -> handleAssignDelivery());
        filterDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            filterByDate(newDate);
        });



        menuIcon.setCursor(Cursor.HAND);
        verifIcon.setCursor(Cursor.HAND);
        deliveryIcon.setCursor(Cursor.HAND);
        addDeliveryButton.setCursor(Cursor.HAND);
        updateButton.setCursor(Cursor.HAND);

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
    private void setupColumns() {
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("idOrder"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
        scheduleCol.setCellValueFactory(new PropertyValueFactory<>("deliverySchedule"));
        deliveryCol.setCellValueFactory(new PropertyValueFactory<>("idPengiriman"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("namaStatus"));
    }
    private void filterByDate(LocalDate date) {
        if (date == null) {
            orderTable.setItems(orderList);
            return;
        }

        ObservableList<orderData> filtered = FXCollections.observableArrayList();
        for (orderData o: orderList) {
            if (o.getTanggalPengiriman().equals(date)) {
                filtered.add(o);
            }
        }
        orderTable.setItems(filtered);
    }
    private void loadOrders() {
        orderList.clear();
        String query = """
            SELECT o.id_order, p.nama_pelanggan, o.total_harga, o.delivery_schedule, 
                   o.id_pengiriman, s.status, o.tanggal_order
            FROM "ORDER" o
            JOIN PELANGGAN p ON o.id_pelanggan = p.id_pelanggan
            JOIN STATUS s ON o.id_status = s.id_status
            WHERE o.id_cabang = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, SessionCabang.getIdCabang());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orderData order = new orderData(
                        rs.getInt("id_order"),
                        rs.getString("nama_pelanggan"),
                        rs.getDouble("total_harga"),
                        rs.getString("delivery_schedule"),
                        rs.getInt("id_pengiriman"),
                        rs.getString("status"),
                        rs.getDate("tanggal_order").toLocalDate()
                );
                System.out.println(order.getIdOrder() + " - " + order.getNamaPelanggan());

                orderList.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        orderTable.setItems(orderList);

    }
    private void loadDeliveryOptions() {
        deliveryDropdown.getItems().clear();
        String query = "SELECT id_pengiriman FROM PENGIRIMAN WHERE id_cabang = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, SessionCabang.getIdCabang());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deliveryDropdown.getItems().add(rs.getInt("id_pengiriman"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStatusOptions() {
        statusDropdown.getItems().clear();
        String query = "SELECT status FROM STATUS";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statusDropdown.getItems().add(rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void handleAssignDelivery() {
        orderData selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select an order to assign delivery.");
            return;
        }

        Integer selectedDeliveryId = deliveryDropdown.getValue();
        if (selectedDeliveryId == null) {
            showAlert("Please select a delivery ID.");
            return;
        }

        String sql = "UPDATE \"ORDER\" SET id_pengiriman = ? WHERE id_order = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedDeliveryId);
            stmt.setInt(2, selected.getIdOrder());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Delivery assigned successfully.");
                loadOrders(); // refresh
            } else {
                showAlert("Failed to assign delivery.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }
    private void handleUpdateStatus() {
        orderData selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select an order to update status.");
            return;
        }

        String selectedStatus = statusDropdown.getValue();
        if (selectedStatus == null) {
            showAlert("Please select a status.");
            return;
        }

        int statusId = getStatusIdByName(selectedStatus);
        if (statusId == -1) {
            showAlert("Invalid status.");
            return;
        }

        String sql = "UPDATE \"ORDER\" SET id_status = ? WHERE id_order = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, statusId);
            stmt.setInt(2, selected.getIdOrder());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Status updated successfully.");
                loadOrders(); // refresh
            } else {
                showAlert("Failed to update status.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }
    private int getStatusIdByName(String statusName) {
        String query = "SELECT id_status FROM STATUS WHERE status = ?::status_enum";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, statusName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id_status");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
