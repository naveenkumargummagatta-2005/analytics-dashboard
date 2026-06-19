package com.dashboard.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Reads DB credentials from db.properties and hands out JDBC connections.
 * Keeping this in one place means the rest of the app never deals with
 * connection strings directly.
 */
public class DatabaseConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static Properties props;

    private static synchronized Properties loadProps() {
        if (props == null) {
            props = new Properties();
            try (InputStream in = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream(CONFIG_FILE)) {
                if (in == null) {
                    throw new RuntimeException(
                        "db.properties not found on classpath. " +
                        "Make sure it sits next to the compiled classes (e.g. in /bin or src root).");
                }
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load db.properties", e);
            }
        }
        return props;
    }

    public static Connection getConnection() throws SQLException {
        Properties p = loadProps();
        String url = p.getProperty("db.url");
        String user = p.getProperty("db.user");
        String password = p.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
