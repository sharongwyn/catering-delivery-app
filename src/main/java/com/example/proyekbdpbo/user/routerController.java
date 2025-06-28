package com.example.proyekbdpbo.user;

import com.example.proyekbdpbo.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class routerController {
    @FXML
    private StackPane container;

    private int orderId;

    public void setOrderId(int orderId) {
        this.orderId = orderId;

        String status = fetchStatusFromDatabase(orderId);
        int stage = mapStatusToStage(status);
        loadStage(stage);
    }

    private String fetchStatusFromDatabase(int orderId) {
        String status = "Waiting for Confirmation"; // default
        String query = """
            SELECT s.status
            FROM "ORDER" o
            JOIN STATUS s ON o.id_status = s.id_status
            WHERE o.id_order = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                status = rs.getString("status");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching status: " + e.getMessage());
        }

        return status;
    }

    private int mapStatusToStage(String status) {
        if (status == null) return 1;

        switch (status.toLowerCase()) {
            case "waiting for confirmation":
                return 1;
            case "processing order":
                return 2;
            case "on delivery":
                return 3;
            case "delivered":
                return 4;
            default:
                return 1;
        }
    }

    private void loadStage(int stage) {
        String fxmlPath = switch (stage) {
            case 1 -> "/com/example/proyekbdpbo/user-status1-view.fxml";
            case 2 -> "/com/example/proyekbdpbo/user-status2-view.fxml";
            case 3 -> "/com/example/proyekbdpbo/user-status3-view.fxml";
            case 4 -> "/com/example/proyekbdpbo/user-status4o-view.fxml";
            default -> "/com/example/proyekbdpbo/user-status1-view.fxml";
        };

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            Object controller = loader.getController();

            if (controller instanceof status1Controller s1) {
                s1.setOrderId(orderId);
            } else if (controller instanceof status2Controller s2) {
                s2.setOrderId(orderId);
            } else if (controller instanceof status3Controller s3) {
                s3.setOrderId(orderId);
            } else if (controller instanceof status4oController s4) {
                s4.setOrderId(orderId);
            }

            container.getChildren().setAll(content);

        } catch (IOException e) {
            System.err.println("Failed to load FXML for stage " + stage + ": " + e.getMessage());
        }
    }
}
