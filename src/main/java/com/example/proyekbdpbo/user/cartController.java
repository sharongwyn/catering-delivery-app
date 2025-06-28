package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.cartitem;
import com.example.proyekbdpbo.model.cartstorage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;

import java.sql.*;

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

    private final double DISCOUNT_PERCENT = 0.2;
    private final double DELIVERY_CHARGE = 0.0;

    @FXML
    public void initialize() {

    }

    public void setupCart() {
        cartItems = cartstorage.getItems();

        itemCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        priceCol.setCellValueFactory(cellData -> {
            cartitem item = cellData.getValue();
            double totalPrice = item.getPrice() * item.getQuantity();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(totalPrice);
        });

        orderTable.setItems(cartItems);

        selectDelivery.setItems(FXCollections.observableArrayList("07.00", "12.00", "18.00"));
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
            showAlert("Order placed successfully!");
        });

        updatePriceSummary();
    }

    private void updatePriceSummary() {
        double subtotal = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double discountAmount = subtotal * DISCOUNT_PERCENT;
        double total = subtotal - discountAmount + DELIVERY_CHARGE;

        subtotalPrice.setText(String.format("Rp %.2f", subtotal));
        discount.setText(String.format("Rp %.2f", discountAmount));
        deliveryCharge.setText(String.format("Rp %.2f", DELIVERY_CHARGE));
        totalPrice.setText(String.format("Rp %.2f", total));
    }

    private void insertToDatabase() {
        double subtotal = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        double discountAmount = subtotal * DISCOUNT_PERCENT;
        double total = subtotal - discountAmount + DELIVERY_CHARGE;
        String payment = selectPayment.getValue();
        String delivery = selectDelivery.getValue();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlTransaksi = "INSERT INTO transaksi (subtotal, discount, delivery_charge, total, payment_method, delivery_time) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement psTrans = conn.prepareStatement(sqlTransaksi, Statement.RETURN_GENERATED_KEYS);
            psTrans.setDouble(1, subtotal);
            psTrans.setDouble(2, discountAmount);
            psTrans.setDouble(3, DELIVERY_CHARGE);
            psTrans.setDouble(4, total);
            psTrans.setString(5, payment);
            psTrans.setString(6, delivery);
            psTrans.executeUpdate();

            ResultSet rs = psTrans.getGeneratedKeys();
            int transaksiId = -1;
            if (rs.next()) {
                transaksiId = rs.getInt(1);
            }

            String sqlDetail = "INSERT INTO detail_transaksi (transaksi_id, menu_name, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
            for (cartitem item : cartItems) {
                psDetail.setInt(1, transaksiId);
                psDetail.setString(2, item.getName());
                psDetail.setInt(3, item.getQuantity());
                psDetail.setDouble(4, item.getPrice());
                psDetail.addBatch();
            }
            psDetail.executeBatch();


        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to save order: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
