import java.sql.*;

public class DB {
    static final String URL = "jdbc:mysql://localhost:3306/cafe_db";
    static final String USER = "root";   // << set your DB user
    static final String PASS = "user_password";       // << set your DB password

    static Connection getCon() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found: " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
