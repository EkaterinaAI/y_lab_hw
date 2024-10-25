package ru.habittracker.service;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.*;
import ru.habittracker.BaseHabitTest;
import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.User;
import ru.habittracker.service.impl.UserService;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link UserService}.
 * <p>
 * Проверяет корректность логики управления пользователями.
 * </p>
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public class UserServiceTest extends BaseHabitTest {

    private static DatabaseConnectionManager dbManager;
    private static IUserService userService;

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

        userService = new UserService(dbManager);
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

        // Создание тестового пользователя
        Optional<User> userOptional = userService.registerUser("testuser@example.com", "password123", "Test User");
        assertTrue(userOptional.isPresent(), "User should be successfully created.");
    }

    /**
     * Тест регистрации нового пользователя.
     */
    @Test
    @DisplayName("Тест регистрации нового пользователя")
    public void testRegisterUser() {
        Optional<User> userOptional = userService.registerUser("newuser@example.com", "newpassword", "New User");
        assertTrue(userOptional.isPresent(), "User should be successfully registered.");
        User user = userOptional.get();
        assertNotNull(user.getId(), "User ID should not be null.");
        assertEquals("newuser@example.com", user.getEmail(), "User email should match.");
        assertEquals("New User", user.getName(), "User name should match.");
    }

    /**
     * Тест регистрации пользователя с существующим email.
     */
    @Test
    @DisplayName("Тест регистрации пользователя с существующим email")
    public void testRegisterUserWithExistingEmail() {
        Optional<User> userOptional = userService.registerUser("testuser@example.com", "password123", "Test User Duplicate");
        assertFalse(userOptional.isPresent(), "Registration with existing email should fail.");
    }

    /**
     * Тест успешного входа в систему.
     */
    @Test
    @DisplayName("Тест успешного входа в систему")
    public void testLoginUser() {
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "User should successfully log in.");
        User user = userOptional.get();
        assertEquals("Test User", user.getName(), "User name should match.");
    }

    /**
     * Тест входа в систему с некорректным паролем.
     */
    @Test
    @DisplayName("Тест входа в систему с некорректным паролем")
    public void testLoginUserWithIncorrectPassword() {
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "wrongpassword");
        assertFalse(userOptional.isPresent(), "Login with incorrect password should fail.");
    }

    /**
     * Тест входа несуществующего пользователя.
     */
    @Test
    @DisplayName("Тест входа несуществующего пользователя")
    public void testLoginNonexistentUser() {
        Optional<User> userOptional = userService.loginUser("nonexistent@example.com", "password123");
        assertFalse(userOptional.isPresent(), "Login of nonexistent user should fail.");
    }

    /**
     * Тест обновления данных пользователя.
     */
    @Test
    @DisplayName("Тест обновления данных пользователя")
    public void testUpdateUser() {
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "User should be found.");
        User user = userOptional.get();

        boolean isUpdated = userService.updateUser(user.getId(), "updated@example.com", "newpassword", "Updated User");
        assertTrue(isUpdated, "User should be successfully updated.");

        Optional<User> updatedUserOptional = userService.loginUser("updated@example.com", "newpassword");
        assertTrue(updatedUserOptional.isPresent(), "Updated user should successfully log in.");
        User updatedUser = updatedUserOptional.get();
        assertEquals("Updated User", updatedUser.getName(), "User name should be updated.");
    }

    /**
     * Тест удаления пользователя.
     */
    @Test
    @DisplayName("Тест удаления пользователя")
    public void testDeleteUser() {
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "User should be found.");
        User user = userOptional.get();

        boolean isDeleted = userService.deleteUser(user.getId());
        assertTrue(isDeleted, "User should be successfully deleted.");

        Optional<User> deletedUserOptional = userService.loginUser("testuser@example.com", "password123");
        assertFalse(deletedUserOptional.isPresent(), "Deleted user should not be able to log in.");
    }
}
