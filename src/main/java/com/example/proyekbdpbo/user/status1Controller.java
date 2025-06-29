package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.orderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class status1Controller {
    private int orderId;
    @FXML
    private TableView<orderItem> orderTable;

    @FXML
    private TableColumn<orderItem, Integer> colNo;

    @FXML
    private TableColumn<orderItem, String> colMenu;

    @FXML
    private TableColumn<orderItem, Integer> colQuantity;

    @FXML
    private TableColumn<orderItem, Double> colPrice;

    @FXML
    private Button backBtn;


    public void setOrderId(int orderId) {
        this.orderId = orderId;
        loadOrderDetails();
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnMouseClicked(e -> goBack());
    }

    private void loadOrderDetails() {
        ObservableList<orderItem> orderItems = FXCollections.observableArrayList();

        String query = """
            SELECT m.nama_menu, d.jumlah, d.harga
            FROM detail_order d
            JOIN menu_harian m ON d.id_menuHarian = m.id_menuHarian
            JOIN "ORDER" o ON d.id_order = o.id_order
            WHERE d.id_order = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            int counter = 1;
            while (rs.next()) {
                String menuName = rs.getString("nama_menu");
                int quantity = rs.getInt("jumlah");
                double price = rs.getDouble("harga");

                orderItems.add(new orderItem(counter++, menuName, quantity, price));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colMenu.setCellValueFactory(new PropertyValueFactory<>("menuName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        orderTable.setItems(orderItems);
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
