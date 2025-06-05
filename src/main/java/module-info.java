module com.example.proyekbdpbo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.proyekbdpbo to javafx.fxml;
    exports com.example.proyekbdpbo;
}