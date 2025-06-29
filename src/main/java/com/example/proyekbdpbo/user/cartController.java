package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.cartitem;
import com.example.proyekbdpbo.model.cartstorage;
import com.example.proyekbdpbo.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class cartController {
    @FXML private TableView<cartitem> orderTable;
    @FXML private TableColumn<cartitem, String> itemCol;
    @FXML private TableColumn<cartitem, Integer> quantityCol;
    @FXML private TableColumn<cartitem, Double> priceCol;

    @FXML private Button deleteButton;
    @FXML private Button orderButton;

    @FXML private ChoiceBox<String> selectDelivery;
    @FXML private ChoiceBox<String> selectPayment;

    @FXML private Label subtotalPrice;
    @FXML private Label discount;
    @FXML private Label deliveryCharge;
    @FXML private Label totalPrice;

    private ObservableList<cartitem> cartItems = FXCollections.observableArrayList();

    private double discountPercent = 0.0;
    private double deliveryChargeAmount = 0.0;
    private Integer idPromosi = null;

    @FXML
    public void initialize() {
    }

    public void setupCart() {
        cartItems = cartstorage.getItems();

        itemCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(cellData -> {
            cartitem item = cellData.getValue();
            double totalPrice = item.getPrice() * item.getQuantity();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(totalPrice);
        });

        orderTable.setItems(cartItems);

        selectDelivery.setItems(FXCollections.observableArrayList("pagi", "siang", "sore"));
        selectPayment.setItems(FXCollections.observableArrayList("Transfer", "Tunai"));

        deleteButton.setOnAction(e -> {
            cartitem selected = orderTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cartItems.remove(selected);
                updatePriceSummary();
            }
        });

        orderButton.setOnAction(e -> {
            if (selectDelivery.getValue() == null || selectPayment.getValue() == null) {
                showAlert("Please select delivery time and payment method.");
                return;
            }
            if (cartItems.isEmpty()) {
                showAlert("Cart is empty.");
                return;
            }

            insertToDatabase();
            cartstorage.clearCart();
            updatePriceSummary();
        });

        updatePriceSummary();
    }

    private void updatePriceSummary() {
        discountPercent = fetchValidDiscountPercentage();
        deliveryChargeAmount = fetchDeliveryChargeByUserCity();

        double subtotal = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double discountAmount = subtotal * discountPercent;
        double total = subtotal - discountAmount + deliveryChargeAmount;

        subtotalPrice.setText(String.format("Rp %.2f", subtotal));
        discount.setText(String.format("Rp %.2f", discountAmount));
        deliveryCharge.setText(String.format("Rp %.2f", deliveryChargeAmount));
        totalPrice.setText(String.format("Rp %.2f", total));
    }

    private double fetchValidDiscountPercentage() {
        double promo = 0.0;
        String query = "SELECT id_promosi, potongan_promo FROM promosi WHERE ? BETWEEN tanggal_promoBerlaku AND tanggal_promoBerakhir LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                idPromosi = rs.getInt("id_promosi");
                promo = rs.getDouble("potongan_promo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return promo;
    }

    private double fetchDeliveryChargeByUserCity() {
        double ongkos = 0.0;
        String query = "SELECT o.ongkos FROM ongkos o JOIN pelanggan p ON o.wilayah = p.kota::text WHERE p.id_user = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Session.getIdPelanggan());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ongkos = rs.getDouble("ongkos");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ongkos;
    }

    private void insertToDatabase() {
        double subtotal = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        double discountAmount = subtotal * discountPercent;
        double total = subtotal - discountAmount + deliveryChargeAmount;
        String delivery = selectDelivery.getValue();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ambil id_cabang dari pelanggan
            int idCabang = -1;
            String sqlCabang = "SELECT id_cabang FROM cabang WHERE nama_cabang = (SELECT kota::text FROM pelanggan WHERE id_user = ?)";
            try (PreparedStatement psCabang = conn.prepareStatement(sqlCabang)) {
                psCabang.setInt(1, Session.getIdPelanggan());
                ResultSet rsCabang = psCabang.executeQuery();
                if (rsCabang.next()) {
                    idCabang = rsCabang.getInt("id_cabang");
                }
            }

            // Ambil id_pelanggan berdasarkan id_user
            int idPelanggan = -1;
            String sqlGetPelanggan = "SELECT id_pelanggan FROM pelanggan WHERE id_user = ?";
            try (PreparedStatement psGet = conn.prepareStatement(sqlGetPelanggan)) {
                psGet.setInt(1, Session.getIdPelanggan());
                ResultSet rsGet = psGet.executeQuery();
                if (rsGet.next()) {
                    idPelanggan = rsGet.getInt("id_pelanggan");
                } else {
                    showAlert("Pelanggan tidak ditemukan!");
                    return;
                }
            }


            String sqlOrder = "INSERT INTO \"ORDER\" (id_pelanggan, id_promosi, id_status, id_cabang, total_harga, ongkos, delivery_schedule) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_order";
            try (PreparedStatement psOrder = conn.prepareStatement(sqlOrder)) {
                psOrder.setInt(1, idPelanggan);
                if (idPromosi != null) {
                    psOrder.setInt(2, idPromosi);
                } else {
                    psOrder.setNull(2, Types.INTEGER);
                }
                psOrder.setInt(3, 1); // id_status default misalnya "menunggu konfirmasi"
                psOrder.setInt(4, idCabang);
                psOrder.setDouble(5, total);
                psOrder.setDouble(6, deliveryChargeAmount);
                psOrder.setObject(7, delivery, java.sql.Types.OTHER);

                ResultSet rsOrder = psOrder.executeQuery();
                int orderId = -1;
                if (rsOrder.next()) {
                    orderId = rsOrder.getInt("id_order");
                }

                String sqlDetail = "INSERT INTO DETAIL_ORDER (id_order, id_menuHarian, jumlah, harga) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (cartitem item : cartItems) {
                        psDetail.setInt(1, orderId);
                        psDetail.setInt(2, item.getId());
                        psDetail.setInt(3, item.getQuantity());
                        psDetail.setDouble(4, item.getPrice());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }

                // Hitung poin: 10% dari total, dibulatkan ke int
                int earnedPoints = (int) Math.round(total * 0.10);

                String updatePointSQL = "UPDATE MEMBER SET point_member = point_member + ? WHERE id_pelanggan = ?";
                try (PreparedStatement psPoint = conn.prepareStatement(updatePointSQL)) {
                    psPoint.setInt(1, earnedPoints);
                    psPoint.setInt(2, idPelanggan);
                    psPoint.executeUpdate();
                }

            }

            showAlert("Order placed successfully!");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-history-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) orderButton.getScene().getWindow();
            stage.setScene(new Scene(root));


        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to save order: " + e.getMessage());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }
}
