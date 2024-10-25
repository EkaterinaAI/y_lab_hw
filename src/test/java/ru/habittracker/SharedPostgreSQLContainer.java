package ru.habittracker;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Класс для предоставления общего контейнера PostgreSQL для всех тестов.
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public class SharedPostgreSQLContainer {

    private static final String IMAGE_VERSION = "postgres:latest";
    private static PostgreSQLContainer<?> container;

    /**
     * Возвращает единственный экземпляр контейнера PostgreSQL.
     *
     * @return экземпляр {@link PostgreSQLContainer}
     */
    public static PostgreSQLContainer<?> getInstance() {
        if (container == null) {
            container = new PostgreSQLContainer<>(IMAGE_VERSION)
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("password")
                    .withInitScript("init.sql");
            container.start();
        }
        return container;
    }
}
