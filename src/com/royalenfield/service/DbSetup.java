package com.royalenfield.service;

/**
 * One-time database setup — run from project: {@code java -cp ".;..\lib\*" com.royalenfield.service.DbSetup}
 */
public final class DbSetup {

    public static void main(String[] args) {
        System.out.println("Royal Enfield — Database Setup");
        System.out.println("Config file: " + DBConnection.getConfigFile().getAbsolutePath());
        System.out.println("Host: " + DBConnection.getHost() + ":" + DBConnection.getPort());
        System.out.println("Database: " + DBConnection.getDatabase());
        System.out.println("User: " + DBConnection.getUser());
        System.out.println();

        try {
            DBConnection.loadConfig();
            DBConnection.ensureDatabaseExists();
            System.out.println("[OK] Database created (if it did not exist)");

            DBConnection.initializeSchema();
            System.out.println("[OK] Tables created");

            System.out.println();
            System.out.println("--- Statistics ---");
            System.out.println("Customers:       " + DBConnection.countCustomers());
            System.out.println("Service records: " + DBConnection.countBikesServiced());
            System.out.println("Active services: " + DBConnection.countActiveServices());
            System.out.println("Revenue:         " + DBConnection.totalRevenue());
            System.out.println();
            System.out.println("Setup complete. Run Main.java to start the app.");
        } catch (Exception ex) {
            System.err.println("[FAILED] " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
