package ru.habittracker.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppConfigTest {

    @Test
    public void testLoadProperties() {
        AppConfig appConfig = new AppConfig();

        assertNotNull(appConfig.getDbUrl(), "Database URL should not be null.");
        assertNotNull(appConfig.getDbUsername(), "Database username should not be null.");
        assertNotNull(appConfig.getDbPassword(), "Database password should not be null.");
        assertNotNull(appConfig.getDbDriver(), "Database driver should not be null.");
        assertNotNull(appConfig.getLiquibaseChangeLog(), "Liquibase changelog file should not be null.");
        assertNotNull(appConfig.getLiquibaseDefaultSchema(), "Liquibase default schema should not be null.");
    }
}
