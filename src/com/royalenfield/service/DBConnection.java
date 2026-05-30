package com.royalenfield.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * JDBC connection manager — reads {@code db.properties} from project root.
 */
public final class DBConnection {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "re_service_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static final Properties config = new Properties();
    private static Connection connection;
    private static boolean driverLoaded;
    private static String lastError = "";

    static {
        loadConfig();
    }

    private DBConnection() {
    }

    public static File getConfigFile() {
        String base = System.getProperty("user.dir");
        File root = new File(base);
        if (new File(root, "db.properties").exists()) {
            return new File(root, "db.properties");
        }
        if (root.getName().equals("src")) {
            return new File(root.getParentFile(), "db.properties");
        }
        return new File(root, "db.properties");
    }

    public static void loadConfig() {
        config.clear();
        config.setProperty("host", DEFAULT_HOST);
        config.setProperty("port", DEFAULT_PORT);
        config.setProperty("database", DEFAULT_DB);
        config.setProperty("user", DEFAULT_USER);
        config.setProperty("password", DEFAULT_PASSWORD);

        File file = getConfigFile();
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                Properties loaded = new Properties();
                loaded.load(in);
                for (String key : new String[]{"host", "port", "database", "user", "password"}) {
                    String v = loaded.getProperty(key);
                    if (v != null && !v.isBlank()) {
                        config.setProperty(key, v.trim());
                    }
                }
            } catch (IOException ex) {
                lastError = "Could not read db.properties: " + ex.getMessage();
            }
        }
        applyEnvOverrides();
    }

    private static void applyEnvOverrides() {
        overrideFromEnv("RE_DB_HOST", "host");
        overrideFromEnv("RE_DB_PORT", "port");
        overrideFromEnv("RE_DB_NAME", "database");
        overrideFromEnv("RE_DB_USER", "user");
        overrideFromEnv("RE_DB_PASSWORD", "password");
    }

    private static void overrideFromEnv(String envKey, String propKey) {
        String value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            config.setProperty(propKey, value.trim());
        }
    }

    public static void applyConfig(String host, String port, String database, String user, String password) {
        config.setProperty("host", host);
        config.setProperty("port", port);
        config.setProperty("database", database);
        config.setProperty("user", user);
        config.setProperty("password", password);
        reconnect();
    }

    public static void saveConfig(String host, String port, String database, String user, String password)
            throws IOException {
        applyConfig(host, port, database, user, password);

        Properties out = new Properties();
        out.setProperty("host", host);
        out.setProperty("port", port);
        out.setProperty("database", database);
        out.setProperty("user", user);
        out.setProperty("password", password);

        File file = getConfigFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            out.store(fos, "Royal Enfield Service Management - Database Settings");
        }
    }

    public static String getHost() {
        return config.getProperty("host", DEFAULT_HOST);
    }

    public static String getPort() {
        return config.getProperty("port", DEFAULT_PORT);
    }

    public static String getDatabase() {
        return config.getProperty("database", DEFAULT_DB);
    }

    public static String getUser() {
        return config.getProperty("user", DEFAULT_USER);
    }

    public static String getPassword() {
        return config.getProperty("password", DEFAULT_PASSWORD);
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabase()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static String getServerJdbcUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort()
                + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static synchronized void reconnect() {
        close();
        connection = null;
    }

    public static synchronized Connection getConnection() throws SQLException {
        loadDriver();
        lastError = "";
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            ensureDatabaseExists();
            connection = DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
        }
        return connection;
    }

    public static boolean isConnected() {
        try {
            Connection c = getConnection();
            return c != null && !c.isClosed() && c.isValid(2);
        } catch (SQLException ex) {
            lastError = ex.getMessage();
            return false;
        }
    }

    public static String getLastError() {
        return lastError.isEmpty() ? "Unknown database error" : lastError;
    }

    public static boolean testConnection() {
        try {
            reconnect();
            getConnection();
            return true;
        } catch (SQLException ex) {
            lastError = ex.getMessage();
            return false;
        }
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            connection = null;
        }
    }

    private static void loadDriver() throws SQLException {
        if (!driverLoaded) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException ex) {
                throw new SQLException(
                        "MySQL JDBC driver not found. Place mysql-connector-j.jar in the lib folder.",
                        ex);
            }
        }
    }

    /**
     * Creates the database if it does not exist (requires MySQL server running).
     */
    public static void ensureDatabaseExists() throws SQLException {
        loadDriver();
        try (Connection server = DriverManager.getConnection(getServerJdbcUrl(), getUser(), getPassword());
             Statement st = server.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + getDatabase() + "`");
        }
    }

    public static void initializeSchema() throws SQLException {
        ensureDatabaseExists();
        getConnection();
        String[] ddl = {
                """
                CREATE TABLE IF NOT EXISTS customers (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(120) NOT NULL,
                    phone VARCHAR(20),
                    email VARCHAR(120),
                    address VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS service_records (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    customer_id INT,
                    bike_model VARCHAR(80),
                    service_type VARCHAR(80),
                    status VARCHAR(40) DEFAULT 'Pending',
                    amount DECIMAL(10,2) DEFAULT 0,
                    service_date DATE,
                    notes TEXT,
                    CONSTRAINT fk_customer FOREIGN KEY (customer_id)
                        REFERENCES customers(id) ON DELETE CASCADE
                )
                """
        };
        try (Statement st = getConnection().createStatement()) {
            for (String sql : ddl) {
                st.executeUpdate(sql);
            }
        }
        seedSampleDataIfEmpty();
    }

    private static void seedSampleDataIfEmpty() {
        if (countCustomers() > 0) {
            return;
        }
        try {
            int c1 = CustomerDAO.insert(new Customer(0, "Arjun Mehta", "9876543210",
                    "arjun@email.com", "12 MG Road, Pune"));
            int c2 = CustomerDAO.insert(new Customer(0, "Priya Sharma", "9123456780",
                    "priya@email.com", "45 Brigade Road, Bangalore"));
            int c3 = CustomerDAO.insert(new Customer(0, "Rahul Verma", "9988776655",
                    "rahul@email.com", "78 Park Street, Kolkata"));

            ServiceRecord s1 = new ServiceRecord();
            s1.setCustomerId(c1);
            s1.setBikeModel("Classic 350");
            s1.setServiceType("General Service");
            s1.setStatus("Completed");
            s1.setAmount(2500);
            s1.setServiceDate(java.time.LocalDate.now().minusDays(5));
            s1.setNotes("Oil change, chain lubrication");
            ServiceDAO.insert(s1);

            ServiceRecord s2 = new ServiceRecord();
            s2.setCustomerId(c2);
            s2.setBikeModel("Hunter 350");
            s2.setServiceType("Engine Tune-up");
            s2.setStatus("In Progress");
            s2.setAmount(4200);
            s2.setServiceDate(java.time.LocalDate.now());
            s2.setNotes("Spark plug replacement pending");
            ServiceDAO.insert(s2);

            ServiceRecord s3 = new ServiceRecord();
            s3.setCustomerId(c3);
            s3.setBikeModel("Meteor 350");
            s3.setServiceType("Brake Service");
            s3.setStatus("Pending");
            s3.setAmount(1800);
            s3.setServiceDate(java.time.LocalDate.now().plusDays(2));
            s3.setNotes("Front brake pads");
            ServiceDAO.insert(s3);
        } catch (Exception ex) {
            System.err.println("[DB] Sample data: " + ex.getMessage());
        }
    }

    // ── Dashboard statistics ────────────────────────────────────────

    public static int countCustomers() {
        return scalarInt("SELECT COUNT(*) FROM customers");
    }

    public static int countBikesServiced() {
        return scalarInt("SELECT COUNT(*) FROM service_records");
    }

    public static int countActiveServices() {
        return scalarInt(
                "SELECT COUNT(*) FROM service_records WHERE status IN ('Pending', 'In Progress')");
    }

    public static double totalRevenue() {
        return scalarDouble(
                "SELECT COALESCE(SUM(amount), 0) FROM service_records WHERE status = 'Completed'");
    }

    private static int scalarInt(String sql) {
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return 0;
    }

    private static double scalarDouble(String sql) {
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException ex) {
            lastError = ex.getMessage();
        }
        return 0.0;
    }

    public static PreparedStatement prepare(String sql) throws SQLException {
        return getConnection().prepareStatement(sql);
    }
}
