package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import com.example.proyekbdpbo.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Date;

public class historyController {
    @FXML
    private ImageView homeIcon, orderIcon, historyIcon, profileIcon;

    @FXML
    private VBox orderContainer;

    @FXML
    public void initialize() {
        homeIcon.setCursor(Cursor.HAND);
        orderIcon.setCursor(Cursor.HAND);
        historyIcon.setCursor(Cursor.HAND);
        profileIcon.setCursor(Cursor.HAND);

        homeIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-home-view.fxml"));
        orderIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-order-view.fxml"));
        historyIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-history-view.fxml"));
        profileIcon.setOnMouseClicked(e -> switchScene("/com/example/proyekbdpbo/user-profile-view.fxml"));
        loadOrderHistory();
    }

    private void switchScene(String fxmlPath) {
        try {
            Stage stage = (Stage) homeIcon.getScene().getWindow(); // ambil window dari salah satu ikon
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOrderHistory() {
        String sql = """
    SELECT
        o.id_order,
        s.status,
        c.nama_cabang AS branch_name,
        o.tanggal_order AS order_date,
        SUM(d.jumlah) AS total_items,
        o.total_harga AS total_price
    FROM "ORDER" o
    JOIN detail_order d ON o.id_order = d.id_order
    LEFT JOIN admin_cabang ac ON o.id_adminCabang = ac.id_adminCabang
    JOIN cabang c ON o.id_cabang = c.id_cabang
    JOIN status s ON o.id_status = s.id_status
    WHERE o.id_pelanggan = ?
    GROUP BY o.id_order, s.status, o.tanggal_order, o.total_harga, c.nama_cabang
    ORDER BY o.tanggal_order DESC
""";


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int userId = getIdPelangganByIdUser(Session.getIdPelanggan());
            stmt.setInt(1, userId);
            System.out.println(userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("id_order");
                String status = rs.getString("status");
                String branch = rs.getString("branch_name");
                int totalItems = rs.getInt("total_items");
                int totalPrice = rs.getInt("total_price");
                Date date = rs.getDate("order_date");
                String formattedDate = date.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

                HBox orderItem = createOrderSummary(orderId, status, branch, formattedDate, totalItems, totalPrice);
                orderContainer.getChildren().add(orderItem);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getIdPelangganByIdUser(int idUser) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id_pelanggan FROM pelanggan WHERE id_user = ?")) {
            stmt.setInt(1, idUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id_pelanggan");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }



    private HBox createOrderSummary(int orderId, String status, String branch, String date, int totalItems, int totalPrice) {
        HBox box = new HBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #FFFCF7; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #3e2c23;");
        box.setPrefWidth(280);

        VBox textBox = new VBox(5);
        textBox.getChildren().addAll(
                createLabel("Branch: " + branch, 14, Color.web("#4a4a4a")),
                createLabel("Date: " + date, 12, Color.web("#6e6e6e")),
                createLabel("Total Items: " + totalItems, 12, Color.web("#6e6e6e"))
        );

        Label priceLabel = createLabel("Rp" + totalPrice, 14, Color.web("#5b8c5a"));
        priceLabel.setStyle("-fx-font-weight: bold;");

        Button detailButton = new Button("Details");
        detailButton.setStyle("-fx-background-color: #5b8c5a; -fx-text-fill: white; -fx-background-radius: 6;");
        detailButton.setFont(Font.font(11));
        detailButton.setCursor(Cursor.HAND);

        detailButton.setOnAction(e -> {
            LocalDateTime orderDateTime = null;
            String status1 = "";
            boolean hasOrderRating = false;
            boolean hasBranchRating = false;

            String queryOrder = """
        SELECT o.tanggal_order, s.status
        FROM "ORDER" o
        JOIN STATUS s ON o.id_status = s.id_status
        WHERE o.id_order = ?
    """;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(queryOrder)) {

                stmt.setInt(1, orderId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    orderDateTime = rs.getTimestamp("tanggal_order").toLocalDateTime();
                    status1 = rs.getString("status");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                return;
            }

            // Cek apakah order sudah lebih dari 24 jam
            if (orderDateTime != null &&
                    Duration.between(orderDateTime, LocalDateTime.now()).toHours() >= 24) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Order has been completed!");
                alert.showAndWait();
                return;
            }

            // Kalau status DELIVERED, cek apakah sudah kasih rating ORDER dan CABANG
            if (status.equalsIgnoreCase("delivered")) {
                try (Connection conn = DatabaseConnection.getConnection()) {

                    // Cek rating order
                    String checkOrderRating = "SELECT COUNT(*) FROM RATING_ORDER WHERE id_order = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(checkOrderRating)) {
                        stmt.setInt(1, orderId);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            hasOrderRating = true;
                        }
                    }

                    int idCabang = -1;
                    int idPelanggan = -1;

                    String getOrderData = "SELECT id_cabang, id_pelanggan FROM \"ORDER\" WHERE id_order = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(getOrderData)) {
                        stmt.setInt(1, orderId);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            idCabang = rs.getInt("id_cabang");
                            idPelanggan = rs.getInt("id_pelanggan");
                        }
                    }

                    hasBranchRating = false;
                    if (idCabang != -1 && idPelanggan != -1) {
                        String checkBranchRating = "SELECT COUNT(*) FROM RATING_CABANG WHERE id_cabang = ? AND id_pelanggan = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(checkBranchRating)) {
                            stmt.setInt(1, idCabang);
                            stmt.setInt(2, idPelanggan);
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next() && rs.getInt(1) > 0) {
                                hasBranchRating = true;
                            }
                        }
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return;
                }

                try {
                    FXMLLoader loader;
                    Parent root;

                    if (!hasOrderRating) {
                        loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-status4o-view.fxml"));
                    } else if (!hasBranchRating) {
                        loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/user-status4b-view.fxml"));
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information");
                        alert.setHeaderText(null);
                        alert.setContentText("Order has been completed!");
                        alert.showAndWait();
                        return;
                    }

                    root = loader.load();

                    Object controller = loader.getController();
                    if (controller instanceof status4oController s4o) {
                        s4o.setOrderId(orderId);
                    } else if (controller instanceof status4bController s4b) {
                        s4b.setOrderId(orderId);
                    }

                    Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Jika belum delivered, buka router normal
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbdpbo/router-view.fxml"));
                    Parent root = loader.load();

                    routerController controller = loader.getController();
                    controller.setOrderId(orderId);

                    Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        VBox rightBox = new VBox(12);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.getChildren().addAll(priceLabel, detailButton);

        HBox.setHgrow(textBox, Priority.ALWAYS);
        box.getChildren().addAll(textBox, rightBox);

        return box;
    }


    private Label createLabel(String text, int fontSize, Color color) {
        Label label = new Label(text);
        label.setFont(new Font("Arial", fontSize));
        label.setTextFill(color);
        return label;
    }
}

