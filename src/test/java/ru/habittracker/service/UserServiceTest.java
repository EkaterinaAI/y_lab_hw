package ru.habittracker.service;

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
import ru.habittracker.repository.UserRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class UserServiceTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password")
            .withInitScript("init.sql"); // Указание скрипта инициализации

    private static DatabaseConnectionManager dbManager;
    private static UserService userService;

    @BeforeAll
    public static void globalSetUp() throws SQLException, LiquibaseException {
        // Инициализируем DatabaseConnectionManager с параметрами тестового контейнера
        dbManager = new DatabaseConnectionManager(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getDriverClassName()
        );

        try (Connection connection = dbManager.getConnection()) {
            // Устанавливаем search_path на service для текущего соединения
            connection.createStatement().execute("SET search_path TO service");

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            // Устанавливаем схемы для Liquibase
            database.setDefaultSchemaName("service");
            database.setLiquibaseSchemaName("service");

            Liquibase liquibase = new Liquibase(
                    "changelog-test.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );
            liquibase.update("");
        }

        // Инициализируем UserService с тестовым DatabaseConnectionManager
        userService = new UserService(dbManager);
    }

    @BeforeEach
    public void setUp() throws SQLException {
        // Создаем пользователя перед каждым тестом
        Optional<User> userOptional = userService.registerUser("testuser@example.com", "password123", "Test User");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть успешно создан.");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Очищаем базу данных после каждого теста
        try (Connection connection = dbManager.getConnection()) {
            connection.createStatement().execute(
                    "TRUNCATE TABLE service.habit_records, service.habits, service.users RESTART IDENTITY CASCADE;"
            );
        }
    }

    @Test
    public void testRegisterUser() {
        Optional<User> userOptional = userService.registerUser("newuser@example.com", "newpassword", "New User");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть успешно зарегистрирован.");
        User user = userOptional.get();
        assertNotNull(user.getId(), "ID пользователя не должен быть null.");
        assertEquals("newuser@example.com", user.getEmail(), "Email пользователя должен совпадать.");
        assertEquals("New User", user.getName(), "Имя пользователя должно совпадать.");
    }

    @Test
    public void testRegisterUserWithExistingEmail() {
        // Попытка зарегистрировать пользователя с уже существующим email
        Optional<User> userOptional = userService.registerUser("testuser@example.com", "password123", "Test User Duplicate");
        assertFalse(userOptional.isPresent(), "Регистрация пользователя с существующим email должна не удаться.");
    }

    @Test
    public void testLoginUser() {
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "Пользователь должен успешно войти.");
        User user = userOptional.get();
        assertEquals("Test User", user.getName(), "Имя пользователя должно совпадать.");
    }

    @Test
    public void testLoginUserWithIncorrectPassword() {
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "wrongpassword");
        assertFalse(userOptional.isPresent(), "Вход с неправильным паролем должен не удаться.");
    }

    @Test
    public void testLoginNonexistentUser() {
        Optional<User> userOptional = userService.loginUser("nonexistent@example.com", "password123");
        assertFalse(userOptional.isPresent(), "Вход несуществующего пользователя должен не удаться.");
    }
}
