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

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class status4oController {
    private int orderId;

    @FXML
    private Button submitButton;

    @FXML
    private TextField reviewInput;

    @FXML
    private ImageView star1, star2, star3, star4, star5;

    @FXML
    private Button skipButton;

    @FXML
    private Button backBtn;

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

    @FXML
    private void handleSubmitRating() {
        String review = reviewInput.getText();
        int rating = currentRating;

        if (rating == 0) {
            showAlert("Please fill in the rating first!");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Koneksi database gagal.");
                return;
            }

            String sql = "INSERT INTO RATING_ORDER (id_order, id_pelanggan, rating_order, ulasan_order) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, orderId);
            stmt.setInt(2, 1);
            stmt.setInt(3, rating);
            if (review == null || review.trim().isEmpty()) {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(4, review);
            }

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Order rating has been saved successfully!");
            }

            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-status4b-view.fxml"));
            Parent root = loader.load();

            status4bController controller = loader.getController();
            controller.setOrderId(orderId);

            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSkipButton(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-status4b-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            status4bController controller = loader.getController();
            controller.setOrderId(orderId);

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
