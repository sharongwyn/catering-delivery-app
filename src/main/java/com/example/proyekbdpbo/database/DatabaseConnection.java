package com.example.proyekbdpbo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    public static Connection databaseLink;

    public static Connection getConnection(){

        String databaseName = "proyek-bd-pbo";
        String user = "postgres";
        String password = "admin";
        String url = "jdbc:postgresql://localhost:5432/" + databaseName;

        try {
            Class.forName("org.postgresql.Driver");
            databaseLink = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection to the database failed.");
            e.printStackTrace();
        }

        return databaseLink;
    }

    public static Statement getStatement() throws SQLException {
        return getConnection().createStatement();
    }
}

