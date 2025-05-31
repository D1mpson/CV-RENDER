package com.example.cvgenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

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
            if (cleanUrl.startsWith("postgres://")) {
                cleanUrl = cleanUrl.replace("postgres://", "postgresql://");
            }

            System.out.println("🔧 Clean URL: " + cleanUrl);

            URI uri = new URI(cleanUrl);

            String host = uri.getHost();
            int port = uri.getPort();
            String database = uri.getPath().substring(1);
            String[] userInfo = uri.getUserInfo().split(":");
            String username = userInfo[0];
            String password = userInfo.length > 1 ? userInfo[1] : "";

            // Визначаємо SSL режим в залежності від середовища
            String sslMode = "disable"; // За замовчуванням для локальної розробки

            // Якщо це продакшн (Supabase або інший хмарний провайдер)
            if (!host.equals("localhost") && !host.equals("127.0.0.1")) {
                sslMode = "require";
            }

            // Формуємо JDBC URL з відповідним SSL режимом
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=%s&prepareThreshold=0&cachePrepStmts=false",
                    host, port, database, sslMode);

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

            // Fallback для локальної розробки БЕЗ SSL
            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/cv?sslmode=disable&prepareThreshold=0")
                    .username("postgres")
                    .password("dimpsonteam2256")
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }
    }
}