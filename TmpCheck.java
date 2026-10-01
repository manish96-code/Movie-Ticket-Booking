public class TmpCheck {
    public static void main(String[] args) {
        com.cinemats.config.DBConnection.initDatabase();
        com.cinemats.dao.BookingDAO.initBookingsTables();
        System.out.println("DB schema ready");
    }
}
