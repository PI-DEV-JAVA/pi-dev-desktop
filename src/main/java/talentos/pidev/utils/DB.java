package talentos.pidev.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private final String URL= "jdbc:mysql://localhost:3306/pidevjava";
    private final String USER= "root";
    private final String PSW ="";

    private Connection myConnection;

    private static DB instance;

    private DB(){
        try {
            myConnection = DriverManager.getConnection(URL,USER,PSW);
            System.out.println("Connection établie!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getMyConnection() {
        return myConnection;
    }

    public static DB getInstance() {
        if(instance == null)
            instance = new DB();
        return instance;
    }
}
