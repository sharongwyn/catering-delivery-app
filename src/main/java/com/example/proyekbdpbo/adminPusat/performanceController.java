package com.example.proyekbdpbo.adminPusat;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.branch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

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

    private ObservableList<branch> branchList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        branchNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        avgRatingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));

        loadBranches();

        viewdetailButton.setOnAction(e -> openDetailView());
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
//                branchList.add(new branch(id, name, rating));
            }
            branchTable.setItems(branchList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDetailView() {
        branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/view/adminp-performance-branch-view.fxml"));
                Parent root = loader.load();

                performancebranchController controller = loader.getController();
//                controller.setBranch(selected);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
