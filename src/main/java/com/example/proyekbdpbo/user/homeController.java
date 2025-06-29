package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.model.menu;
import com.example.proyekbdpbo.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class homeController {

    @FXML
    private Label titleHomeView;

    @FXML
    private Label menu1name, menu2name, menu3name;

    @FXML
    private ImageView menu1image, menu2image, menu3image;

    @FXML
    private ImageView homeIcon, orderIcon, historyIcon, profileIcon;

    @FXML
    private Label cityLabel;


    private void switchScene(String fxmlPath) {
        try {
            Stage stage = (Stage) homeIcon.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        titleHomeView.setText("Hi " + Session.getNamaPelanggan() + "!");

        ArrayList<menu> menus = getMenusByUserCity();
        showMenus(menus);
        String userCity = getUserCity();
        if (userCity != null) {
            cityLabel.setText(userCity);
        }


        homeIcon.setCursor(Cursor.HAND);
        orderIcon.setCursor(Cursor.HAND);
        historyIcon.setCursor(Cursor.HAND);
        profileIcon.setCursor(Cursor.HAND);

        homeIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-home-view.fxml"));
        orderIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-order-view.fxml"));
        historyIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-history-view.fxml"));
        profileIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-profile-view.fxml"));
    }

    private String getUserCity() {
        String city = null;
        String query = "SELECT kota FROM pelanggan WHERE id_user = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Session.getIdPelanggan()); // atau Session.getIdUser() tergantung yang kamu simpan
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                city = rs.getString("kota");
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching user city");
            e.printStackTrace();
        }

        return city;
    }


    private ArrayList<menu> getMenusByUserCity() {
        ArrayList<menu> menuList = new ArrayList<>();
        String query = """
                SELECT mh.id_menuHarian,mh.nama_menu, mh.image_path
                           FROM PELANGGAN p
                           JOIN CABANG c ON p.kota::text = c.nama_cabang
                           JOIN MENU_HARIAN_CABANG mhc ON c.id_cabang = mhc.id_cabang
                           JOIN MENU_HARIAN mh ON mh.id_menuHarian = mhc.id_menuHarian
                           WHERE p.id_user = ? AND mhc.tanggal_menu = CURRENT_DATE + INTERVAL '1 day'
                           ORDER BY mhc.tanggal_menu DESC
                           LIMIT 3
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Session.getIdPelanggan());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_menuHarian");
                String name = rs.getString("nama_menu");
                String imagePath = rs.getString("image_path");
                menuList.add(new menu(id,name, imagePath));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching menus by user city");
            e.printStackTrace();
        }

        return menuList;
    }

    private void showMenus(ArrayList<menu> menus) {
        menu1name.setText("");
        menu1image.setImage(null);
        menu2name.setText("");
        menu2image.setImage(null);
        menu3name.setText("");
        menu3image.setImage(null);

        for (int i = 0; i < menus.size(); i++) {
            menu m = menus.get(i);
            String menuName = m.getName();
            String imageFileName = m.getImagePath();

            Image image = null;
            try {
                URL resourceUrl = getClass().getResource("/images/" + imageFileName);
                if (resourceUrl != null) {
                    String imagePath = resourceUrl.toExternalForm();
                    image = new Image(imagePath);
                } else {
                    System.out.println("❌ Gambar tidak ditemukan: " + imageFileName);
                }
            } catch (Exception e) {
                System.out.println("Gagal load gambar: " + imageFileName);
                e.printStackTrace();
            }

            if (i == 0) {
                menu1name.setText(menuName);
                if (image != null) menu1image.setImage(image);
            } else if (i == 1) {
                menu2name.setText(menuName);
                if (image != null) menu2image.setImage(image);
            } else if (i == 2) {
                menu3name.setText(menuName);
                if (image != null) menu3image.setImage(image);
            }
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