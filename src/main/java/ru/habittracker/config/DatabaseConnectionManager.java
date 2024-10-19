package ru.habittracker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {

    private final String url;
    private final String username;
    private final String password;
    private final String driver;

    // Конструктор по умолчанию
    public DatabaseConnectionManager() {
        AppConfig config = new AppConfig();
        this.url = config.getDbUrl();
        this.username = config.getDbUsername();
        this.password = config.getDbPassword();
        this.driver = config.getDbDriver();
    }

    // Конструктор для тестов
    public DatabaseConnectionManager(String url, String username, String password, String driver) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driver = driver;
    }

    public Connection getConnection() {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
    }
}
