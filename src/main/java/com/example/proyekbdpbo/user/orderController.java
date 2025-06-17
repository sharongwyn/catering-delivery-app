package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.branchwithmenus;
import com.example.proyekbdpbo.model.menu;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class orderController {
    @FXML
    private ChoiceBox<branchwithmenus> selectBranch;

    @FXML
    private ImageView r1m1image, r1m2image, r2m1image, r2m2image, r3m1image, r3m2image;
    @FXML
    private Label r1m1name, r1m2name, r2m1name, r2m2name, r3m1name, r3m2name;
    @FXML
    private Label r1m1price, r1m2price, r2m1price, r2m2price, r3m1price, r3m2price;
    @FXML
    private Button r1m1view, r1m2view, r2m1view, r2m2view, r3m1view, r3m2view;

    @FXML
    private Button cartButton;

    @FXML
    public void initialize() {
        ArrayList<branchwithmenus> branchList = getBranchesFromDB();
        selectBranch.getItems().addAll(branchList);

        if (!branchList.isEmpty()) {
            selectBranch.setValue(branchList.get(0)); // default pilih Surabaya (index 0)

            branchwithmenus selected = selectBranch.getValue();
            if (selected != null) {
                loadMenusForBranch(selected); // agar menus terisi
                showMenus(selected.getMenus()); // tampilkan menunya
            }
        }

        // Listener saat user memilih branch
        selectBranch.setOnAction(event -> {
            branchwithmenus selected = selectBranch.getValue();
            if (selected != null) {
                selected.getMenus().clear(); // reset jika sebelumnya sudah pernah dimuat
                loadMenusForBranch(selected);
                showMenus(selected.getMenus());
            }
        });

        // Set action semua tombol view
        r1m1view.setOnAction(e -> handleMenuView(0));
        r1m2view.setOnAction(e -> handleMenuView(1));
        r2m1view.setOnAction(e -> handleMenuView(2));
        r2m2view.setOnAction(e -> handleMenuView(3));
        r3m1view.setOnAction(e -> handleMenuView(4));
        r3m2view.setOnAction(e -> handleMenuView(5));

        cartButton.setOnAction(e -> handleViewCart());

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
                b.setId(id);
                branches.add(b);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching branches");
            e.printStackTrace();
        }

        return branches;
    }


    private void loadMenusForBranch(branchwithmenus branch) {
        String query = "SELECT * FROM menus WHERE branch_id = " + branch.getId();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String menuName = rs.getString("name");
                String image = rs.getString("image_path");
                double price = rs.getDouble("price");
                String description = rs.getString("description");

                menu m = new menu(menuName, image, price, description);
                branch.addMenu(m);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching menus");
            e.printStackTrace();
        }
    }

    private void showMenus(ArrayList<menu> menus) {
        if (menus.size() > 0) {
            r1m1name.setText(menus.get(0).getName());
            r1m1price.setText("Rp " + menus.get(0).getPrice());
            r1m1image.setImage(loadImage(menus.get(0).getImagePath()));
        }
        if (menus.size() > 1) {
            r1m2name.setText(menus.get(1).getName());
            r1m2price.setText("Rp " + menus.get(1).getPrice());
            r1m2image.setImage(loadImage(menus.get(1).getImagePath()));
        }
        if (menus.size() > 2) {
            r2m1name.setText(menus.get(2).getName());
            r2m1price.setText("Rp " + menus.get(2).getPrice());
            r2m1image.setImage(loadImage(menus.get(2).getImagePath()));
        }
        if (menus.size() > 3) {
            r2m2name.setText(menus.get(3).getName());
            r2m2price.setText("Rp " + menus.get(3).getPrice());
            r2m2image.setImage(loadImage(menus.get(3).getImagePath()));
        }
        if (menus.size() > 4) {
            r3m1name.setText(menus.get(4).getName());
            r3m1price.setText("Rp " + menus.get(4).getPrice());
            r3m1image.setImage(loadImage(menus.get(4).getImagePath()));
        }
        if (menus.size() > 5) {
            r3m2name.setText(menus.get(5).getName());
            r3m2price.setText("Rp " + menus.get(5).getPrice());
            r3m2image.setImage(loadImage(menus.get(5).getImagePath()));
        }
    }

    private Image loadImage(String fileName) {
        try {
            return new Image(getClass().getResourceAsStream("/images/" + fileName));
        } catch (Exception e) {
            System.out.println("Error loading image: " + fileName);
            return null;
        }
    }


    private void handleMenuView(int index) {
        branchwithmenus selected = selectBranch.getValue();
        if (selected != null && selected.getMenus().size() > index) {
            menu m = selected.getMenus().get(index);
            openMenuDetailWindow(m);
        }
    }

    private void openMenuDetailWindow(menu m) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-order-permenu-view.fxml"));
            Parent root = loader.load();
            permenuController controller = loader.getController();
            controller.setMenu(m); // jika kamu passing data

//            permenuController controller = loader.getController();
//            controller.setMenu(m);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu Detail");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-cart-view.fxml"));
            Parent root = loader.load();

            // Ambil controllernya
            cartController controller = loader.getController();
            controller.setupCart();  // <-- panggil ini!

            Stage stage = new Stage();
            stage.setTitle("Your Cart");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

