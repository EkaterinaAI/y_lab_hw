package ru.habittracker.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link ru.habittracker.config.AppConfig}.
 * <p>
 * Проверяет корректность загрузки конфигурационных свойств приложения.
 * </p>
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public class AppConfigTest {

    /**
     * Тест загрузки всех свойств конфигурации.
     */
    @Test
    @DisplayName("Проверка загрузки всех свойств конфигурации приложения")
    public void testLoadProperties() {
        AppConfig appConfig = new AppConfig();

        assertNotNull(appConfig.getDbUrl(), "Database URL should not be null.");
        assertNotNull(appConfig.getDbUsername(), "Database username should not be null.");
        assertNotNull(appConfig.getDbPassword(), "Database password should not be null.");
        assertNotNull(appConfig.getDbDriver(), "Database driver should not be null.");
        assertNotNull(appConfig.getLiquibaseChangeLog(), "Liquibase changelog file should not be null.");
    }
}
