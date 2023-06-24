package TaskManagerProject;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Connections {
    public static java.sql.Connection getConnection() {
        try {
            java.sql.Connection con = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/task_management_system?serverTimezone=UTC", "root", "password");
            return con;
        } catch (SQLException e) {
        	System.out.println(e.toString());
            throw new RuntimeException(e);
        }
    }
}
