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
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=%s&prepareThreshold=0&cachePrepStmts=false&tcpKeepAlive=true",
                    host, port, database, sslMode);

            System.out.println("🔗 Host: " + host);
            System.out.println("🔗 Port: " + port);
            System.out.println("🔗 Database: " + database);
            System.out.println("👤 Username: " + username);
            System.out.println("🔒 Password length: " + password.length());
            System.out.println("🔗 SSL Mode: " + sslMode);
            System.out.println("🔗 JDBC URL: " + jdbcUrl);

            // КРИТИЧНО: Налаштування HikariCP для Supabase Free tier
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");

            // Мінімальні налаштування для Supabase Free tier
            config.setMaximumPoolSize(1);
            config.setMinimumIdle(0);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(120000);     // 2 хвилини замість 5
            config.setMaxLifetime(300000);     // 5 хвилин замість 10
            config.setLeakDetectionThreshold(0); // Відключаємо для економії ресурсів
            config.setInitializationFailTimeout(-1); // Не фейлити одразу

            // Додаткові оптимізації для Supabase
            config.addDataSourceProperty("tcpKeepAlive", "true");
            config.addDataSourceProperty("socketTimeout", "20");
            config.addDataSourceProperty("loginTimeout", "10");
            config.addDataSourceProperty("cancelSignalTimeout", "10");
            config.addDataSourceProperty("connectTimeout", "10");

            // КРИТИЧНО: Додаємо retry механізм
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);

            HikariDataSource ds = new HikariDataSource(config);

            System.out.println("✅ HikariDataSource створено з оптимізованими налаштуваннями");
            System.out.println("📊 Max Pool Size: " + config.getMaximumPoolSize());
            System.out.println("📊 Min Idle: " + config.getMinimumIdle());

            return ds;

        } catch (Exception e) {
            System.err.println("❌ Critical Error with DATABASE_URL: " + e.getMessage());
            e.printStackTrace();

            // ВИДАЛЕНО FALLBACK - якщо Supabase не працює, краще зупинити додаток
            // ніж намагатися підключитися до неіснуючого localhost

            throw new RuntimeException("Не вдалося підключитися до бази даних. " +
                    "Перевірте DATABASE_URL та доступність Supabase. Error: " + e.getMessage(), e);
        }
    }
}