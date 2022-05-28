package carsharing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) {
        ArrayList<String> options = new ArrayList<>(List.of(args));
        String dbFile = options.contains("-databaseFileName") ? args[options.indexOf("-databaseFileName") + 1] : "db";
        String dbPath = "./src/carsharing/db/" + dbFile; //Car Sharing/task/src/
        final String JDBC_DRIVER = "org.h2.Driver";
        final String DB_URL = "jdbc:h2:" + dbPath;

        try (
                Connection connection = DriverManager.getConnection(DB_URL);
                Statement statement = connection.createStatement();
                ){
            Class.forName(JDBC_DRIVER);
            connection.setAutoCommit(true);
            String sql =  "CREATE TABLE COMPANY(ID INT NOT NULL, NAME VARCHAR)";
            statement.executeUpdate(sql);
        } catch(SQLException | ClassNotFoundException se) {
            se.printStackTrace();
        }
    }

}