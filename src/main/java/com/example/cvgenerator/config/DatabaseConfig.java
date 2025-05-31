package com.example.cvgenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:postgresql://postgres:dimpsonteam2256@localhost:5432/cv}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        System.out.println("🔧 Raw DATABASE_URL: " + databaseUrl);

        try {
            // Парсимо URL вручну
            String cleanUrl = databaseUrl;

            // Neon зазвичай використовує postgres://, але Spring Boot потребує postgresql://
            if (cleanUrl.startsWith("postgres://")) {
                cleanUrl = cleanUrl.replace("postgres://", "postgresql://");
            }

            System.out.println("🔧 Clean URL: " + cleanUrl);

            URI uri = new URI(cleanUrl);

            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : 5432; // Default PostgreSQL port
            String database = uri.getPath().substring(1); // Видаляємо перший символ '/'

            String username;
            String password;

            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":");
                username = userInfo[0];
                password = userInfo.length > 1 ? userInfo[1] : "";
            } else {
                // Fallback для локальної розробки
                username = "postgres";
                password = "dimpsonteam2256";
            }

            // Визначаємо SSL режим
            String sslMode = "require"; // За замовчуванням require для продакшн

            // Тільки для localhost вимикаємо SSL
            if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
                sslMode = "disable";
            }

            // Формуємо JDBC URL з правильними параметрами для Neon
            String jdbcUrl = String.format(
                    "jdbc:postgresql://%s:%d/%s?sslmode=%s&prepareThreshold=0&cachePrepStmts=false&useSSL=true&requireSSL=true&verifyServerCertificate=false",
                    host, port, database, sslMode
            );

            System.out.println("🔗 Host: " + host);
            System.out.println("🔗 Port: " + port);
            System.out.println("🔗 Database: " + database);
            System.out.println("👤 Username: " + username);
            System.out.println("🔒 Password length: " + password.length());
            System.out.println("🔗 SSL Mode: " + sslMode);
            System.out.println("🔗 JDBC URL: " + jdbcUrl);

            DataSource ds = DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();

            System.out.println("✅ DataSource створено успішно");
            return ds;

        } catch (Exception e) {
            System.err.println("❌ Error parsing DATABASE_URL: " + e.getMessage());
            e.printStackTrace();

            // Fallback тільки для локальної розробки
            System.out.println("🔄 Використовується fallback конфігурація для локальної розробки");
            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/cv?sslmode=disable&prepareThreshold=0")
                    .username("postgres")
                    .password("dimpsonteam2256")
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }
    }
}