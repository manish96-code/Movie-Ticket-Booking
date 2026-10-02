import java.sql.*;

public class TmpCheck {
    public static void main(String[] args) {
        com.cinemats.config.DBConnection.initDatabase();
        try (Connection conn = com.cinemats.config.DBConnection.getConnection();
             Statement s = conn.createStatement()) {
            for (String tbl : new String[]{"users", "categories", "movies", "screens", "screen_seats", "shows", "show_prices", "show_seats", "bookings", "tickets"}) {
                try (ResultSet rs = s.executeQuery("SELECT count(*) FROM " + tbl)) {
                    if (rs.next()) System.out.println(tbl + ": " + rs.getInt(1));
                } catch (Exception e) {
                    System.out.println(tbl + ": error (" + e.getMessage() + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
