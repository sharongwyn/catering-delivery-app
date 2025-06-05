module com.example.proyekbdpbo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.proyekbdpbo to javafx.fxml;
    exports com.example.proyekbdpbo;
}