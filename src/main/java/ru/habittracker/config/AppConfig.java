package ru.habittracker.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс для загрузки и предоставления настроек приложения из файла properties.
 * <p>
 * Загружает настройки базы данных и Liquibase из файла application.properties.
 * </p>
 *
 * author
 *      Ekaterina Ishchuk
 */
public class AppConfig {
    private Properties properties = new Properties();

    /**
     * Загружает настройки из файла application.properties.
     */
    public AppConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Не удалось найти файл application.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Получает URL базы данных.
     *
     * @return URL базы данных
     */
    public String getDbUrl() {
        return properties.getProperty("db.url");
    }

    /**
     * Получает имя пользователя базы данных.
     *
     * @return имя пользователя базы данных
     */
    public String getDbUsername() {
        return properties.getProperty("db.username");
    }

    /**
     * Получает пароль базы данных.
     *
     * @return пароль базы данных
     */
    public String getDbPassword() {
        return properties.getProperty("db.password");
    }

    /**
     * Получает драйвер базы данных.
     *
     * @return драйвер базы данных
     */
    public String getDbDriver() {
        return properties.getProperty("db.driver");
    }

    /**
     * Получает путь к файлу Liquibase changelog.
     *
     * @return путь к файлу changelog
     */
    public String getLiquibaseChangeLog() {
        return properties.getProperty("liquibase.changeLogFile");
    }
}
