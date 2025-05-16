package edu.mci.foodorderbuddy;

import com.vaadin.flow.component.page.AppShellConfigurator;
import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.data.test.TestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@EntityScan("edu.mci.foodorderbuddy") // Ihr Paket mit den Entities
@EnableJpaRepositories("edu.mci.foodorderbuddy") // Ihr Repository-Paket
@EnableTransactionManagement
@SpringBootApplication
@Theme("my-theme")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {

        try {
            String jdbcUrl = "jdbc:postgresql://35.205.245.25:5432/postgres";
            String username = "postgres";
            String password = "foodorderbuddy123";

            System.out.println("🔌 JDBC: " + jdbcUrl);

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {

                if (rs.next()) {
                    System.out.println("✅ Verbindung erfolgreich: " + rs.getInt(1));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Verbindung fehlgeschlagen.");
        }

        SpringApplication.run(Application.class, args);
    }
}
