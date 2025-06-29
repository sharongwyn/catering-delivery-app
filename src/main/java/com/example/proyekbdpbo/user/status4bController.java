package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class status4bController {
    private int orderId;
    @FXML
    private TextField reviewInput;

    @FXML
    private Button submitButton;

    @FXML
    private Button skipButton;

    @FXML
    private Button backBtn;

    @FXML
    private ImageView star1, star2, star3, star4, star5;

    private String starEmpty;
    private String starFull;

    private ImageView[] stars;
    private int currentRating = 0;

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void initialize() {

        URL urlEmpty = getClass().getResource("/images/star_empty.png");
        URL urlFull = getClass().getResource("/images/star_filled.png");

        if (urlEmpty == null || urlFull == null) {
            System.err.println("Gambar tidak ditemukan. Cek path gambar!");
            return;
        }

        starEmpty = urlEmpty.toExternalForm();
        starFull = urlFull.toExternalForm();

        stars = new ImageView[]{star1, star2, star3, star4, star5};

        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnMouseClicked(event -> {
                currentRating = index + 1;
                updateStars(currentRating);
            });
        }

        submitButton.setCursor(Cursor.HAND);
        skipButton.setCursor(Cursor.HAND);
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnMouseClicked(e -> goBack());

    }

    private void updateStars(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImage(new Image(starFull));
            } else {
                stars[i].setImage(new Image(starEmpty));
            }
        }
    }
    private int getCabangIdFromOrder(int idOrder) {
        int idCabang = -1;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_cabang FROM \"ORDER\" WHERE id_order = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idOrder);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                idCabang = rs.getInt("id_cabang");
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Failed to get cabang id: " + e.getMessage());
            e.printStackTrace();
        }

        return idCabang;
    }

    @FXML
    private void handleSubmitBranchRating() {
        String review = reviewInput.getText();
        int rating = currentRating;

        int idPelanggan = getIdPelangganFromUser(Session.getIdPelanggan());
        int idCabang = getCabangIdFromOrder(orderId);

        System.out.println("Rating selected: " + currentRating);
        System.out.println("Cabang ID: " + idCabang);
        System.out.println("Pelanggan ID: " + Session.getIdPelanggan());
        System.out.println("Review: " + review);


        submitButton.setCursor(Cursor.HAND);

        if (rating == 0 || idCabang == -1) {
            System.err.println("Rating or branch not valid. ");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO RATING_CABANG (id_cabang, id_pelanggan, rating_cabang, ulasan_cabang) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, idCabang);
            stmt.setInt(2, idPelanggan);
            stmt.setInt(3, rating);
            if (review == null || review.trim().isEmpty()) {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(4, review);
            }

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Branch rating has been saved successfully!");
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-home-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getIdPelangganFromUser(int idUser) {
        int idPelanggan = -1;
        String query = "SELECT id_pelanggan FROM pelanggan WHERE id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                idPelanggan = rs.getInt("id_pelanggan");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idPelanggan;
    }


    @FXML
    private void handleSkipButton(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-home-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

}
