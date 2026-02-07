package pi_dev.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/main";
    private static final String USER = "pidev";
    private static final String PASSWORD = "pidev";

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            e.printStackTrace();
        }
        return connection;
    }
}
