package com.example.proyekbdpbo.adminPusat;

import com.example.proyekbdpbo.database.DatabaseConnection;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
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

public class menuController {
    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, String> itemCol;
    @FXML private TableColumn<MenuItem, String> descCol;
    @FXML private TableColumn<MenuItem, Double> priceCol;
    @FXML private TableColumn<MenuItem, String> imageCol;
    @FXML private TableColumn<MenuItem, String> categoryCol;

    @FXML private TextField additemField;
    @FXML private TextField adddescField;
    @FXML private TextField addpriceField;
    @FXML private TextField addimageField;
    @FXML private ChoiceBox<String> selectCategory;

    @FXML private Button addButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    @FXML private ImageView promotionIcon;
    @FXML private ImageView menuIcon;
    @FXML private ImageView performanceIcon;

    private ObservableList<MenuItem> menuList = FXCollections.observableArrayList();
    private MenuItem selectedItem = null;

    public static class MenuItem {
        private int id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty description;
        private final SimpleDoubleProperty price;
        private final SimpleStringProperty image;
        private final SimpleStringProperty category;

        public MenuItem(int id, String name, String description, double price, String image, String category) {
            this.id = id;
            this.name = new SimpleStringProperty(name);
            this.description = new SimpleStringProperty(description);
            this.price = new SimpleDoubleProperty(price);
            this.image = new SimpleStringProperty(image);
            this.category = new SimpleStringProperty(category);
        }

        public int getId() { return id; }
        public String getName() { return name.get(); }
        public String getDescription() { return description.get(); }
        public double getPrice() { return price.get(); }
        public String getImage() { return image.get(); }
        public String getCategory() { return category.get(); }
    }

    @FXML
    public void initialize() {
        itemCol.setCellValueFactory(data -> data.getValue().name);
        descCol.setCellValueFactory(data -> data.getValue().description);
        priceCol.setCellValueFactory(data -> data.getValue().price.asObject());
        imageCol.setCellValueFactory(data -> data.getValue().image);
        categoryCol.setCellValueFactory(data -> data.getValue().category);

        selectCategory.setItems(FXCollections.observableArrayList("Food", "Beverage"));
        loadMenuItems();

        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedItem = newVal;
            if (newVal != null) {
                additemField.setText(newVal.getName());
                adddescField.setText(newVal.getDescription());
                addpriceField.setText(String.valueOf(newVal.getPrice()));
                addimageField.setText(newVal.getImage());
                selectCategory.setValue(newVal.getCategory());
            }
        });

        promotionIcon.setCursor(Cursor.HAND);
        menuIcon.setCursor(Cursor.HAND);
        performanceIcon.setCursor(Cursor.HAND);

        promotionIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/adminp-promotion-view.fxml"));
        menuIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/adminp-menu-view.fxml"));
        performanceIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/adminp-performance-view.fxml"));
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

    private void loadMenuItems() {
        menuList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM menu_harian")) {

            while (rs.next()) {
                int id = rs.getInt("id_menuHarian");
                String name = rs.getString("nama_menu");
                String desc = rs.getString("deskripsi");
                double price = rs.getDouble("harga_menu");
                String img = rs.getString("image_path");
                String cat = rs.getString("kategori_menu");
                menuList.add(new MenuItem(id, name, desc, price, img, cat));
            }
            menuTable.setItems(menuList);

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    @FXML
    private void handleAdd() {
        try {
            String name = additemField.getText();
            String desc = adddescField.getText();
            double price = Double.parseDouble(addpriceField.getText());
            String img = addimageField.getText();
            String cat = selectCategory.getValue();

            String query = "INSERT INTO menu_harian (nama_menu, deskripsi, harga_menu, image_path, kategori_menu) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setString(2, desc);
                stmt.setDouble(3, price);
                stmt.setString(4, img);
                stmt.setString(5, cat);
                stmt.executeUpdate();
            }

            clearFields();
            loadMenuItems();
            showAlert(Alert.AlertType.INFORMATION, "Success", "A new menu has been added!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Fail to add menu: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (selectedItem == null) return;

        try {
            String name = additemField.getText();
            String desc = adddescField.getText();
            double price = Double.parseDouble(addpriceField.getText());
            String img = addimageField.getText();
            String cat = selectCategory.getValue();

            String query = "UPDATE menu_harian SET nama_menu=?, deskripsi=?, harga_menu=?, image_path=?, kategori_menu=? WHERE id_menuHarian=?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setString(2, desc);
                stmt.setDouble(3, price);
                stmt.setString(4, img);
                stmt.setString(5, cat);
                stmt.setInt(6, selectedItem.getId());
                stmt.executeUpdate();
            }

            clearFields();
            loadMenuItems();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Menu has been updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Fail to update menu: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedItem == null) return;

        try {
            String query = "DELETE FROM menu_harian WHERE id_menuHarian = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, selectedItem.getId());
                stmt.executeUpdate();
            }

            clearFields();
            loadMenuItems();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Menu has been deleted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Fail to delete menu: " + e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        additemField.clear();
        adddescField.clear();
        addpriceField.clear();
        addimageField.clear();
        selectCategory.setValue(null);
        menuTable.getSelectionModel().clearSelection();
        selectedItem = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
