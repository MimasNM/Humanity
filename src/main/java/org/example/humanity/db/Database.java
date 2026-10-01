package org.example.humanity.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Подключение к базе MySQL.
 * Все настройки берутся из файла db.properties (src/main/resources/db.properties).
 */
public final class Database {

    private static final Properties CONFIG = loadConfig();

    private Database() {
    }

    /**
     * Каждый вызов открывает новое соединение. Закрывать его должен вызывающий код (try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://"
                + CONFIG.getProperty("db.host") + ":"
                + CONFIG.getProperty("db.port") + "/"
                + CONFIG.getProperty("db.name")
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC"
                + "&characterEncoding=utf8";
        return DriverManager.getConnection(url,
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password"));
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("Файл db.properties не найден в resources");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать db.properties", e);
        }
        return properties;
    }
}
