package edu.mci.foodorderbuddy.data.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbTest {

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            System.out.printf("⚠️  Env %s fehlt – verwende Default: %s%n", key, defaultValue);
            return defaultValue;
        }
        return value;
    }

    public static void main(String[] args) {
        try {
            String jdbcUrl = String.format(
                    "jdbc:postgresql:///postgres?cloudSqlInstance=%s&socketFactory=com.google.cloud.sql.postgres.SocketFactory&user=%s&password=%s",
                    "foodorderbuddy:europe-west1:foodorderbuddy-db",
                    "postgres",
                    "foodorderbuddy123"
            );

            System.out.println("🔌 JDBC: " + jdbcUrl);

            try (Connection conn = DriverManager.getConnection(jdbcUrl);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {

                if (rs.next()) {
                    System.out.println("✅ Verbindung erfolgreich: " + rs.getInt(1));
                }

                TestController testcontroller = new TestController();
                testcontroller.test();

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Verbindung fehlgeschlagen.");
        }
    }
}
