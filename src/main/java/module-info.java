module com.example.proyekbdpbo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.proyekbdpbo to javafx.fxml;
    opens com.example.proyekbdpbo.user to javafx.fxml;
    opens com.example.proyekbdpbo.adminPusat to javafx.fxml;
    opens com.example.proyekbdpbo.adminCabang to javafx.fxml;
    opens com.example.proyekbdpbo.database to javafx.fxml;

    exports com.example.proyekbdpbo;
    exports com.example.proyekbdpbo.user;
    exports com.example.proyekbdpbo.adminCabang;
    exports com.example.proyekbdpbo.adminPusat;
    exports com.example.proyekbdpbo.database;

}