package ru.habittracker.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private Properties properties = new Properties();

    public AppConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Извините, не удалось найти файл application.properties");
                return;
            }
            // Загружаем файл свойств
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public String getDbUsername() {
        return properties.getProperty("db.username");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password");
    }

    public String getDbDriver() {
        return properties.getProperty("db.driver");
    }

    public String getLiquibaseChangeLog() {
        return properties.getProperty("liquibase.changeLogFile");
    }

    public String getLiquibaseDefaultSchema() {
        return properties.getProperty("liquibase.defaultSchema");
    }
}
