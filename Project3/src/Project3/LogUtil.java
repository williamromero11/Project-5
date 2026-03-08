package Project3;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

public class LogUtil {

    private final Properties appProps = new Properties();

    public LogUtil(String path) throws Exception {
        try (FileInputStream fis = new FileInputStream(path)) {
            appProps.load(fis);
        }
    }

    private Connection connectLogDb() throws Exception {
        Properties p = new Properties();
        p.setProperty("driver", appProps.getProperty("log.driver"));
        p.setProperty("url", appProps.getProperty("log.url"));
        return DBUtil.connect(p, appProps.getProperty("log.username"), appProps.getProperty("log.password"));
    }

    public void logSuccess(String loginUsername, boolean isQuery) {
        // Only successful ops should count. If logging fails, do NOT break the user app.
        String key = (loginUsername == null || loginUsername.isBlank()) ? "unknown@localhost" : loginUsername;

        String sql =
            "INSERT INTO operationscount (login_username, num_queries, num_updates) " +
            "VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "num_queries = num_queries + VALUES(num_queries), " +
            "num_updates = num_updates + VALUES(num_updates)";

        int qInc = isQuery ? 1 : 0;
        int uInc = isQuery ? 0 : 1;

        try (Connection c = connectLogDb();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setInt(2, qInc);
            ps.setInt(3, uInc);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }
}