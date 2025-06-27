package com.example.proyekbdpbo.adminPusat;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.promotion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;

public class promotionController {
    @FXML private TableView<promotion> promotionTable;
    @FXML private TableColumn<promotion, Integer> idCol;
    @FXML private TableColumn<promotion, Double> discountCol;
    @FXML private TableColumn<promotion, Date> sdateCol;
    @FXML private TableColumn<promotion, Date> edateCol;

    @FXML private TextField discinputField;
    @FXML private DatePicker pickSdate;
    @FXML private DatePicker pickEdate;

    @FXML private Button addButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    private ObservableList<promotion> promotionList = FXCollections.observableArrayList();
    private promotion selectedPromo = null;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        discountCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getDiscount()).asObject());
        sdateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStartDate()));
        edateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getEndDate()));

        loadPromotions();

        promotionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedPromo = newVal;
            if (newVal != null) {
                discinputField.setText(String.valueOf(newVal.getDiscount()));
                pickSdate.setValue(newVal.getStartDate().toLocalDate());
                pickEdate.setValue(newVal.getEndDate().toLocalDate());
            }
        });
    }

    private void loadPromotions() {
        promotionList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM promosi")) {
            while (rs.next()) {
                int id = rs.getInt("id_promosi");
                double discount = rs.getDouble("potongan_promo");
                Date sdate = rs.getDate("tanggal_promoberlaku");
                Date edate = rs.getDate("tanggal_promoberakhir");
                promotionList.add(new promotion(id, discount, sdate, edate));
            }
            promotionTable.setItems(promotionList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        try {
            double discount = Double.parseDouble(discinputField.getText());
            LocalDate sdate = pickSdate.getValue();
            LocalDate edate = pickEdate.getValue();

            if (sdate == null || edate == null) {
                showAlert("Date tidak boleh kosong");
                return;
            }

            String query = "INSERT INTO promosi (potongan_promo, tanggal_promoberlaku, tanggal_promoberakhir) VALUES (?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDouble(1, discount);
                stmt.setDate(2, Date.valueOf(sdate));
                stmt.setDate(3, Date.valueOf(edate));
                stmt.executeUpdate();
            }

            clearFields();
            loadPromotions();
        } catch (NumberFormatException e) {
            showAlert("Masukkan discount berupa angka desimal. Contoh: 0.2");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        if (selectedPromo == null) {
            showAlert("Pilih data yang ingin diedit.");
            return;
        }

        try {
            double discount = Double.parseDouble(discinputField.getText());
            LocalDate sdate = pickSdate.getValue();
            LocalDate edate = pickEdate.getValue();

            if (sdate == null || edate == null) {
                showAlert("Date must be filled!");
                return;
            }

            String query = "UPDATE promosi SET potongan_promo = ?, tanggal_promoberlaku = ?, tanggal_promoberakhir = ? WHERE id_promosi = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDouble(1, discount);
                stmt.setDate(2, Date.valueOf(sdate));
                stmt.setDate(3, Date.valueOf(edate));
                stmt.setInt(4, selectedPromo.getId());
                stmt.executeUpdate();
            }

            clearFields();
            loadPromotions();
        } catch (NumberFormatException e) {
            showAlert("Masukkan discount berupa angka desimal. Contoh: 0.2");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedPromo == null) {
            showAlert("Pilih data yang ingin dihapus.");
            return;
        }

        String query = "DELETE FROM promosi WHERE id_promosi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, selectedPromo.getId());
            stmt.executeUpdate();
            clearFields();
            loadPromotions();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearFields() {
        discinputField.clear();
        pickSdate.setValue(null);
        pickEdate.setValue(null);
        promotionTable.getSelectionModel().clearSelection();
        selectedPromo = null;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
