package ru.habittracker.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionManagerTest {

    @Test
    public void testGetConnection() {
        try (PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
                .withDatabaseName("testdb")
                .withUsername("postgres")
                .withPassword("password")) {

            postgresContainer.start();

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
}
