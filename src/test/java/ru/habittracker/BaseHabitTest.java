package ru.habittracker;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Базовый класс для интеграционных тестов.
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public abstract class BaseHabitTest {

    protected static PostgreSQLContainer<?> postgresContainer = SharedPostgreSQLContainer.getInstance();
}
