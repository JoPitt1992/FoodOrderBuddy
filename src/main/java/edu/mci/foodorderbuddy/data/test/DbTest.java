package edu.mci.foodorderbuddy.data.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbTest {
    public static void main(String[] args) {
        try {
            String jdbcUrl = String.format(
                "jdbc:postgresql:///%s?cloudSqlInstance=%s&socketFactory=com.google.cloud.sql.postgres.SocketFactory&user=%s&password=%s",
                System.getenv("DB_NAME"),
                System.getenv("DB_HOST"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASS")
            );

            Connection conn = DriverManager.getConnection(jdbcUrl);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");

            if (rs.next()) {
                System.out.println("✅ Verbindung erfolgreich: " + rs.getInt(1));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Verbindung fehlgeschlagen.");
        }
    }
}
