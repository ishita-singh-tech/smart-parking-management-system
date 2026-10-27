
import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/smart_parking";
        String user = "root";
        String password = "I#singh1";

        try (Connection con =
                 DriverManager.getConnection(url, user, password);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT COUNT(*) FROM parking_dataset_import")) {

            rs.next();
            System.out.println("MySQL connected successfully!");
            System.out.println("Parking records: " + rs.getInt(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

