package talentos.pidev.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/main";
    private static final String USER = "pidev";
    private static final String PASSWORD = "pidev";

    public static volatile Connection connection;

    private DB(){};

    public static Connection getConnection() throws SQLException   {
        if (connection==null || connection.isClosed()){
            synchronized (DB.class){
                if (connection==null ||connection.isClosed()){
                    connection=DriverManager.getConnection(URL,USER,PASSWORD);
                }
            }
        }
        return connection;
    }
}