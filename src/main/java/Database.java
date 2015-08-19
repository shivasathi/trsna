import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

public class Database {

    public static void insertCountryCounts(long minute, HashMap<String, Integer> counts){
        Connection con = null;
        PreparedStatement pst = null;

        String url = "jdbc:mysql://localhost:3306/twitmap";
        String user = "root";
        String password = "madesimple";

        try {
            con = DriverManager.getConnection(url, user, password);
            for(String country_code : counts.keySet()) {
                pst = con.prepareStatement("INSERT INTO tweets_country_counts VALUES(?,?,?)");
                pst.setLong(1, minute);
                pst.setString(2, country_code);
                pst.setInt(3, counts.get(country_code));
                pst.executeUpdate();
            }

        } catch (SQLException ex) {

        } finally {

            try {
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {

            }
        }
    }
}
