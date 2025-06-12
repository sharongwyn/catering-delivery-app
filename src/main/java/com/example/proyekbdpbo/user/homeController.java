package com.example.proyekbdpbo.user;


import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.branch;
import com.example.proyekbdpbo.model.branchwithmenus;
import com.example.proyekbdpbo.model.menu;
import com.example.proyekbdpbo.model.user;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class homeController {

    @FXML
    private Label titleHomeView;

    @FXML
    private ChoiceBox<branchwithmenus> selectBranch;

    @FXML
    private Label menu1name, menu2name, menu3name;

    @FXML
    private ImageView menu1image, menu2image, menu3image;

    private user userloggedin;

    @FXML
    private HBox menuRec; // Ini bind dari ScrollPane kamu


    @FXML
    public void initialize() {
        userloggedin = new user("desi123", "desi");
        titleHomeView.setText("Hi " + userloggedin.getNama() + "!");

        ArrayList<branchwithmenus> branchList = getBranchesFromDB();
        selectBranch.getItems().addAll(branchList);

//        branchwithmenus branch1 = new branchwithmenus("branch A");
//        branchwithmenus branch2 = new branchwithmenus("branch B");
//
//        branch1.addMenu(new menu("Ayam geprek", "box.png"));
//        branch1.addMenu(new menu("Nasi goreng", "performance.png"));
//        branch1.addMenu(new menu("Air putih", "box.png"));
//
//        branch2.addMenu(new menu("Mie ayam", "marketing.png"));
//        branch2.addMenu(new menu("Air putih", "store.png"));
//        branch2.addMenu(new menu("Nasi lemak", "box.png"));
//
//
//        selectBranch.getItems().addAll(branch1, branch2);

        selectBranch.setOnAction(e -> {
            branchwithmenus selected = selectBranch.getValue();
            if (selected != null) {
                ArrayList<menu> menus = getMenusByBranchId(selected.getId());
                showMenus(menus);
            }
        });

        // ✨ Tampilkan menu dari database ke ScrollPane/HBox
        showMenusFromDB();

    }

    private void showMenus(ArrayList<menu> menus) {
        // Kosongkan HBox dulu
        menuRec.getChildren().clear();

        for (menu m : menus) {
            VBox menuBox = new VBox();
            menuBox.setPrefSize(200, 145);
            menuBox.setSpacing(5);

            ImageView imageView = new ImageView();
            imageView.setFitHeight(140);
            imageView.setFitWidth(200);
            imageView.setPreserveRatio(true);

            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/" + m.getImagePath())));

            } catch (Exception e) {
                System.out.println("Gagal load image: " + m.getImagePath());
            }

            Label nameLabel = new Label(m.getName());
            nameLabel.setPrefSize(200, 36);
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-family: 'Arial Rounded MT Bold';");

            menuBox.getChildren().addAll(imageView, nameLabel);
            menuRec.getChildren().add(menuBox);
        }





    }

    public ArrayList<menu> getMenufromDB() {
        ArrayList<menu> menuList = new ArrayList<>();
        String query = "SELECT name, image_path from menus";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String imagePath = rs.getString("image_path");
                menuList.add(new menu(name, imagePath));


            }

        } catch (SQLException e) {
            System.out.println("Error retrieving menu from database");
            e.printStackTrace();
        }


        return menuList;

    }

    private void showMenusFromDB() {
        ArrayList<menu> menus = getMenufromDB();

        for (menu m : menus) {
            VBox menuBox = new VBox();
            menuBox.setPrefSize(200, 145);
            menuBox.setSpacing(5);

            ImageView imageView = new ImageView();
            imageView.setFitHeight(140);
            imageView.setFitWidth(200);
            imageView.setPreserveRatio(true);

            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/" + m.getImagePath())));
            } catch (Exception e) {
                System.out.println("Gagal load image: " + m.getImagePath());
            }

            Label nameLabel = new Label(m.getName());
            nameLabel.setPrefSize(200, 36);
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-family: 'Arial Rounded MT Bold';");

            menuBox.getChildren().addAll(imageView, nameLabel);
            menuRec.getChildren().add(menuBox); // menuRec itu HBox di ScrollPane kamu
        }


    }


    private ArrayList<branchwithmenus> getBranchesFromDB() {
        ArrayList<branchwithmenus> branches = new ArrayList<>();

        String query = "SELECT * FROM branch";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("name");

                branchwithmenus b = new branchwithmenus(nama);
                b.setId(id); // Pastikan branchwithmenus kamu punya method setId
                branches.add(b);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching branches");
            e.printStackTrace();
        }

        return branches;
    }

    private ArrayList<menu> getMenusByBranchId(int branchId) {
        ArrayList<menu> menuList = new ArrayList<>();
        String query = "SELECT name, image_path FROM menus WHERE branch_id = " + branchId;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String imagePath = rs.getString("image_path");
                menuList.add(new menu(name, imagePath));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching menus by branch_id");
            e.printStackTrace();
        }

        return menuList;
    }

}






