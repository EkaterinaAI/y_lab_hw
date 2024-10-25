package ru.habittracker.repository;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.*;
import ru.habittracker.BaseHabitTest;
import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.User;
import ru.habittracker.repository.impl.UserRepository;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link UserRepository}.
 * <p>
 * Проверяет корректность операций с пользователями в базе данных.
 * </p>
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public class UserRepositoryTest extends BaseHabitTest {

    private static DatabaseConnectionManager dbManager;
    private static IUserRepository userRepository;

    /**
     * Инициализация ресурсов перед всеми тестами.
     *
     * @throws Exception возможное исключение при инициализации
     */
    @BeforeAll
    public static void globalSetUp() throws Exception {
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

    /**
     * Подготовка тестовых данных перед каждым тестом.
     *
     * @throws Exception возможное исключение при подготовке данных
     */
    @BeforeEach
    public void setUp() throws Exception {
        try (Connection connection = dbManager.getConnection()) {
            connection.createStatement().execute(
                    "TRUNCATE TABLE service.users RESTART IDENTITY CASCADE;"
            );
        }
    }

    /**
     * Тест сохранения пользователя.
     */
    @Test
    @DisplayName("Тест сохранения пользователя")
    public void testSaveUser() {
        User user = new User(0, "user@example.com", "password123", "Test User");
        Optional<User> savedUserOptional = userRepository.save(user);

        assertTrue(savedUserOptional.isPresent(), "User should be successfully saved.");
        User savedUser = savedUserOptional.get();
        assertTrue(savedUser.getId() > 0, "User ID should be greater than 0.");
        assertEquals("user@example.com", savedUser.getEmail(), "Emails should match.");
    }

    /**
     * Тест поиска пользователя по email.
     */
    @Test
    @DisplayName("Тест поиска пользователя по email")
    public void testFindByEmail() {
        User user = new User(0, "user@example.com", "password123", "Test User");
        userRepository.save(user);

        Optional<User> foundUserOptional = userRepository.findByEmail("user@example.com");
        assertTrue(foundUserOptional.isPresent(), "User should be found by email.");
        User foundUser = foundUserOptional.get();
        assertEquals("user@example.com", foundUser.getEmail(), "Emails should match.");
    }

    /**
     * Тест поиска пользователя по ID.
     */
    @Test
    @DisplayName("Тест поиска пользователя по ID")
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

    /**
     * Тест обновления информации о пользователе.
     */
    @Test
    @DisplayName("Тест обновления информации о пользователе")
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

    /**
     * Тест удаления пользователя.
     */
    @Test
    @DisplayName("Тест удаления пользователя")
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
