package ru.habittracker.repository;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class UserRepositoryTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password")
            .withInitScript("init.sql");

    private static DatabaseConnectionManager dbManager;
    private static UserRepository userRepository;

    @BeforeAll
    public static void globalSetUp() throws SQLException, LiquibaseException {
        dbManager = new DatabaseConnectionManager(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getDriverClassName()
        );

        try (Connection connection = dbManager.getConnection()) {
            connection.createStatement().execute("SET search_path TO service");

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            database.setDefaultSchemaName("service");
            database.setLiquibaseSchemaName("service");

            Liquibase liquibase = new Liquibase(
                    "changelog-test.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );
            liquibase.update("");
        }

        userRepository = new UserRepository(dbManager);
    }

    @BeforeEach
    public void setUp() throws SQLException {
        // Clean up before each test
        try (Connection connection = dbManager.getConnection()) {
            connection.createStatement().execute(
                    "TRUNCATE TABLE service.users RESTART IDENTITY CASCADE;"
            );
        }
    }

    @Test
    public void testSaveUser() {
        User user = new User(0, "user@example.com", "password123", "Test User");
        Optional<User> savedUserOptional = userRepository.save(user);

        assertTrue(savedUserOptional.isPresent(), "User should be saved successfully.");
        User savedUser = savedUserOptional.get();
        assertTrue(savedUser.getId() > 0, "User ID should be greater than 0.");
        assertEquals("user@example.com", savedUser.getEmail(), "Emails should match.");
    }

    @Test
    public void testFindByEmail() {
        User user = new User(0, "user@example.com", "password123", "Test User");
        userRepository.save(user);

        Optional<User> foundUserOptional = userRepository.findByEmail("user@example.com");
        assertTrue(foundUserOptional.isPresent(), "User should be found by email.");
        User foundUser = foundUserOptional.get();
        assertEquals("user@example.com", foundUser.getEmail(), "Emails should match.");
    }

    @Test
    public void testFindById() {
        User user = new User(0, "user@example.com", "password123", "Test User");
        Optional<User> savedUserOptional = userRepository.save(user);
        assertTrue(savedUserOptional.isPresent(), "User should be saved successfully.");

        int userId = savedUserOptional.get().getId();

        Optional<User> foundUserOptional = userRepository.findById(userId);
        assertTrue(foundUserOptional.isPresent(), "User should be found by ID.");
        User foundUser = foundUserOptional.get();
        assertEquals(userId, foundUser.getId(), "User IDs should match.");
    }

    @Test
    public void testUpdateUser() {
        User user = new User(0, "user@example.com", "password123", "Test User");
        Optional<User> savedUserOptional = userRepository.save(user);
        assertTrue(savedUserOptional.isPresent(), "User should be saved successfully.");

        User savedUser = savedUserOptional.get();
        savedUser.setEmail("newemail@example.com");
        savedUser.setName("Updated Name");

        boolean isUpdated = userRepository.update(savedUser);
        assertTrue(isUpdated, "User should be updated successfully.");

        Optional<User> updatedUserOptional = userRepository.findById(savedUser.getId());
        assertTrue(updatedUserOptional.isPresent(), "Updated user should be found.");
        User updatedUser = updatedUserOptional.get();
        assertEquals("newemail@example.com", updatedUser.getEmail(), "Email should be updated.");
        assertEquals("Updated Name", updatedUser.getName(), "Name should be updated.");
    }

    @Test
    public void testDeleteUser() {
        User user = new User(0, "user@example.com", "password123", "Test User");
        Optional<User> savedUserOptional = userRepository.save(user);
        assertTrue(savedUserOptional.isPresent(), "User should be saved successfully.");

        int userId = savedUserOptional.get().getId();

        boolean isDeleted = userRepository.delete(userId);
        assertTrue(isDeleted, "User should be deleted successfully.");

        Optional<User> deletedUserOptional = userRepository.findById(userId);
        assertFalse(deletedUserOptional.isPresent(), "User should no longer exist.");
    }
}
