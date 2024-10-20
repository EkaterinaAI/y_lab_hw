package ru.habittracker.config;

import org.junit.jupiter.api.Test;
import ru.habittracker.BaseHabitTest;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link ru.habittracker.config.DatabaseConnectionManager}.
 * <p>
 * Проверяет возможность установления соединения с базой данных.
 * </p>
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public class DatabaseConnectionManagerTest extends BaseHabitTest {

    /**
     * Тест успешного получения соединения с базой данных.
     */
    @Test
    public void testGetConnection() {
        DatabaseConnectionManager dbManager = new DatabaseConnectionManager(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getDriverClassName()
        );

        try (Connection connection = dbManager.getConnection()) {
            assertNotNull(connection, "Connection should not be null.");
            assertFalse(connection.isClosed(), "Connection should be open.");
        } catch (SQLException e) {
            fail("SQLException should not occur.");
        }
    }
}
