package com.pi.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection connect() {

        String url = "jdbc:mysql://localhost:3306/projet_pi";
        String user = "root";
        String password = "";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion réussie !");
            return conn;

        } catch (SQLException e) {
            System.out.println("Erreur de connexion !");
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection() {
    }
}
