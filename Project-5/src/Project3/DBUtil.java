package Project3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBUtil {

    public static Connection connect(Properties dbProps, String username, String password) throws Exception {
        String driver = dbProps.getProperty("driver");
        String url = dbProps.getProperty("url");
        if (driver == null || url == null) {
            throw new IllegalArgumentException("DB properties must contain: driver, url");
        }
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    public static boolean isSelect(String sql) {
        if (sql == null) return false;
        String s = sql.trim().toLowerCase();
        return s.startsWith("select");
    }

    public static String normalizeSingleStatement(String sql) {
        if (sql == null) return "";
        return sql.trim().replaceAll(";\\s*$", "");
    }
}