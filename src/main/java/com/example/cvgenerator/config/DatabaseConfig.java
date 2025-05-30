package com.example.cvgenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:postgresql://localhost:5432/cv}")
    private String databaseUrl;

    @Value("${DB_USERNAME:postgres}")
    private String username;

    @Value("${DB_PASSWORD:dimpsonteam2256}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Автоматично додаємо jdbc: якщо його немає
        String jdbcUrl = databaseUrl;
        if (!jdbcUrl.startsWith("jdbc:")) {
            jdbcUrl = "jdbc:" + jdbcUrl;
        }

        System.out.println("🔗 Connecting to: " + jdbcUrl);
        System.out.println("👤 Username: " + username);

        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}