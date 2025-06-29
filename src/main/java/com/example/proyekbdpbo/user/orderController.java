package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.branchwithmenus;
import com.example.proyekbdpbo.model.menu;
import com.example.proyekbdpbo.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private ImageView homeIcon, orderIcon, historyIcon, profileIcon;


    @FXML
    public void initialize() {
        // Ambil cabang sesuai kota pelanggan yang login
        branchwithmenus userBranch = getUserBranch();

        if (userBranch != null) {
            selectBranch.getItems().add(userBranch);
            selectBranch.setValue(userBranch);
            loadMenusForBranch(userBranch);
            showMenus(userBranch.getMenus());
            selectBranch.setDisable(true); // agar tidak bisa diganti
        }

        r1m1view.setOnAction(e -> handleMenuView(0));
        r1m2view.setOnAction(e -> handleMenuView(1));
        r2m1view.setOnAction(e -> handleMenuView(2));
        r2m2view.setOnAction(e -> handleMenuView(3));
        r3m1view.setOnAction(e -> handleMenuView(4));
        r3m2view.setOnAction(e -> handleMenuView(5));

        cartButton.setOnAction(e -> handleViewCart());

        homeIcon.setCursor(Cursor.HAND);
        orderIcon.setCursor(Cursor.HAND);
        historyIcon.setCursor(Cursor.HAND);
        profileIcon.setCursor(Cursor.HAND);

        homeIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-home-view.fxml"));
        orderIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-order-view.fxml"));
        historyIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-history-view.fxml"));
        profileIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-profile-view.fxml"));
    }

    private branchwithmenus getUserBranch() {
        branchwithmenus result = null;
        String query = """
                SELECT c.id_cabang, c.nama_cabang FROM pelanggan p
                JOIN cabang c ON c.nama_cabang = p.kota::text
                WHERE p.id_user = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Session.getIdPelanggan());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id_cabang");
                String nama = rs.getString("nama_cabang");
                result = new branchwithmenus(nama);
                result.setId(id);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching branch for user");
            e.printStackTrace();
        }
        return result;
    }

    private void switchScene(String fxmlPath) {
        try {
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMenusForBranch(branchwithmenus branch) {
        String query = """
        SELECT mh.id_menuHarian, mh.nama_menu, mh.image_path, mh.harga_menu, mh.deskripsi
        FROM pelanggan p
        JOIN cabang c ON p.kota::text = c.nama_cabang
        JOIN menu_harian_cabang mhc ON c.id_cabang = mhc.id_cabang
        JOIN menu_harian mh ON mhc.id_menuharian = mh.id_menuHarian
        WHERE p.id_user = ? AND mhc.tanggal_menu = CURRENT_DATE + INTERVAL '1 day'
        ORDER BY mhc.tanggal_menu DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Session.getIdPelanggan());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_menuHarian");
                String menuName = rs.getString("nama_menu");
                String image = rs.getString("image_path");
                double price = rs.getDouble("harga_menu");
                String description = rs.getString("deskripsi");

                menu m = new menu(id,menuName, image, price, description);
                m.setBranchId(branch.getId());
                branch.addMenu(m);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching menus for branch");
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
            controller.setMenu(m);

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

            cartController controller = loader.getController();
            controller.setupCart();

            Stage stage = new Stage();
            stage.setTitle("Your Cart");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}