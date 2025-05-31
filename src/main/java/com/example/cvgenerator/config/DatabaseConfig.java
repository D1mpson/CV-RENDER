package com.example.cvgenerator.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
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
            int port = uri.getPort() != -1 ? uri.getPort() : 5432;
            String database = uri.getPath().substring(1);

            String username;
            String password;

            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":");
                username = userInfo[0];
                password = userInfo.length > 1 ? userInfo[1] : "";
            } else {
                username = "postgres";
                password = "dimpsonteam2256";
            }

            // Визначаємо SSL режим
            String sslMode = "require";
            if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
                sslMode = "disable";
            }

            // Створюємо HikariConfig вручну для повного контролю
            HikariConfig config = new HikariConfig();

            // Базові налаштування
            config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s?sslmode=%s", host, port, database, sslMode));
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");

            // Налаштування пулу для Neon/Render
            config.setMaximumPoolSize(3);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(180000);
            config.setMaxLifetime(300000);
            config.setLeakDetectionThreshold(60000);

            // КРИТИЧНО: Вимикаємо autoCommit
            config.setAutoCommit(false);

            // Додаткові налаштування для стабільності
            config.addDataSourceProperty("tcpKeepAlive", "true");
            config.addDataSourceProperty("socketTimeout", "30");
            config.addDataSourceProperty("loginTimeout", "15");

            // Валідація з'єднань
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);

            System.out.println("🔗 Host: " + host);
            System.out.println("🔗 Port: " + port);
            System.out.println("🔗 Database: " + database);
            System.out.println("👤 Username: " + username);
            System.out.println("🔒 Password length: " + password.length());
            System.out.println("🔗 SSL Mode: " + sslMode);
            System.out.println("🔗 JDBC URL: " + config.getJdbcUrl());
            System.out.println("🔧 AutoCommit: " + config.isAutoCommit());

            HikariDataSource dataSource = new HikariDataSource(config);
            System.out.println("✅ HikariDataSource створено успішно");

            return dataSource;

        } catch (Exception e) {
            System.err.println("❌ Error creating DataSource: " + e.getMessage());
            e.printStackTrace();

            // Fallback для локальної розробки
            System.out.println("🔄 Використовується fallback конфігурація");
            HikariConfig fallbackConfig = new HikariConfig();
            fallbackConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/cv?sslmode=disable");
            fallbackConfig.setUsername("postgres");
            fallbackConfig.setPassword("dimpsonteam2256");
            fallbackConfig.setDriverClassName("org.postgresql.Driver");
            fallbackConfig.setAutoCommit(false);
            fallbackConfig.setMaximumPoolSize(2);

            return new HikariDataSource(fallbackConfig);
        }
    }
}