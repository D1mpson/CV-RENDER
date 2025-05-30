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

            // Додаємо SSL та відключаємо prepared statements для Supabase pooler
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=require&prepareThreshold=0&cachePrepStmts=false",
                    host, port, database);

            System.out.println("🔗 Host: " + host);
            System.out.println("🔗 Port: " + port);
            System.out.println("🔗 Database: " + database);
            System.out.println("👤 Username: " + username);
            System.out.println("🔒 Password length: " + password.length());
            System.out.println("🔗 JDBC URL: " + jdbcUrl);

            DataSource ds = DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();

            System.out.println("✅ DataSource створено успішно з відключеними prepared statements");
            return ds;

        } catch (Exception e) {
            System.err.println("❌ Error parsing DATABASE_URL: " + e.getMessage());
            e.printStackTrace();
            // Fallback для локальної розробки
            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/cv?prepareThreshold=0")
                    .username("postgres")
                    .password("dimpsonteam2256")
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }
    }
}