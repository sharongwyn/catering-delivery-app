package com.example.proyekbdpbo.adminPusat;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.branch;
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
import java.sql.ResultSet;
import java.sql.Statement;

public class performanceController {
    @FXML
    private TableView<branch> branchTable;
    @FXML private TableColumn<branch, Integer> idCol;
    @FXML private TableColumn<branch, String> branchNameCol;
    @FXML private TableColumn<branch, Double> avgRatingCol;
    @FXML
    private Button viewdetailButton;

    @FXML private ImageView promotionIcon;
    @FXML private ImageView menuIcon;
    @FXML private ImageView performanceIcon;


    private ObservableList<branch> branchList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        branchNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        avgRatingCol.setCellValueFactory(new PropertyValueFactory<>("avgRating"));

        loadBranches();

        viewdetailButton.setOnAction(e -> openDetailView());

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

    private void loadBranches() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT id_cabang, nama_cabang, average_rating FROM cabang");

            while (rs.next()) {
                int id = rs.getInt("id_cabang");
                String name = rs.getString("nama_cabang");
                double rating = rs.getDouble("average_rating");
                branchList.add(new branch(id, name, rating));
            }
            branchTable.setItems(branchList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openDetailView() {
        branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Load FXML dan buat controller
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/adminp-performance-perbranch-view.fxml"));
                Parent root = loader.load();

                // Ambil controller dan kirim data branch
                performancebranchController controller = loader.getController();
                controller.setBranch(selected);

                // Tampilkan di jendela baru
                Stage stage = new Stage();
                stage.setTitle("Branch Performance Detail");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No branch selected.");
        }
    }


}
