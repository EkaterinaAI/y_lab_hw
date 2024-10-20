package ru.habittracker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Управляет подключениями к базе данных.
 * <p>
 * Предоставляет методы для получения соединения с базой данных, используя настройки из AppConfig.
 * </p>
 *
 * author
 *      Ekaterina Ishchuk
 */
public class DatabaseConnectionManager {

    private final String url;
    private final String username;
    private final String password;
    private final String driver;

    /**
     * Конструктор по умолчанию, использующий настройки из AppConfig.
     */
    public DatabaseConnectionManager() {
        AppConfig config = new AppConfig();
        this.url = config.getDbUrl();
        this.username = config.getDbUsername();
        this.password = config.getDbPassword();
        this.driver = config.getDbDriver();
    }

    /**
     * Конструктор для тестирования с пользовательскими параметрами.
     *
     * @param url      URL базы данных
     * @param username имя пользователя
     * @param password пароль
     * @param driver   драйвер базы данных
     */
    public DatabaseConnectionManager(String url, String username, String password, String driver) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driver = driver;
    }

    /**
     * Получает подключение к базе данных.
     *
     * @return объект {@link Connection}
     * @throws RuntimeException если не удалось установить соединение
     */
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
