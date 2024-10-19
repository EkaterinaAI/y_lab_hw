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
import ru.habittracker.model.Habit;
import ru.habittracker.model.User;
import ru.habittracker.repository.HabitRepository;
import ru.habittracker.repository.UserRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class HabitServiceTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password")
            .withInitScript("init.sql"); // Указание скрипта инициализации

    private static DatabaseConnectionManager dbManager;
    private static HabitService habitService;
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

        // Инициализируем сервисы с тестовым DatabaseConnectionManager
        habitService = new HabitService(dbManager);
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
    public void testCreateHabit() {
        // Получаем созданного пользователя
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть найден.");
        User user = userOptional.get();

        Habit habit = habitService.createHabit(user.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Привычка не должна быть null.");
        assertTrue(habit.getId() > 0, "ID привычки должно быть больше 0.");
        assertEquals("Exercise", habit.getTitle(), "Название привычки должно совпадать.");
    }

    @Test
    public void testGetHabits() {
        // Получаем созданного пользователя
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть найден.");
        User user = userOptional.get();

        habitService.createHabit(user.getId(), "Exercise", "Morning exercise", 1);
        habitService.createHabit(user.getId(), "Read", "Read a book", 2);

        List<Habit> habits = habitService.getHabits(user.getId());
        assertEquals(2, habits.size(), "Должно быть создано 2 привычки.");
    }

    @Test
    public void testUpdateHabit() {
        // Получаем созданного пользователя
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть найден.");
        User user = userOptional.get();

        Habit habit = habitService.createHabit(user.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Привычка не должна быть null.");

        boolean isUpdated = habitService.updateHabit(user.getId(), habit.getId(), "Yoga", "Evening yoga", 2);
        assertTrue(isUpdated, "Привычка должна быть успешно обновлена.");

        List<Habit> habits = habitService.getHabits(user.getId());
        Habit updatedHabit = habits.stream().filter(h -> h.getId() == habit.getId()).findFirst().orElse(null);
        assertNotNull(updatedHabit, "Обновленная привычка должна существовать.");
        assertEquals("Yoga", updatedHabit.getTitle(), "Название привычки должно быть обновлено.");
        assertEquals("Evening yoga", updatedHabit.getDescription(), "Описание привычки должно быть обновлено.");
        assertEquals(2, updatedHabit.getFrequency(), "Частота привычки должна быть обновлена.");
    }

    @Test
    public void testDeleteHabit() {
        // Получаем созданного пользователя
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть найден.");
        User user = userOptional.get();

        Habit habit = habitService.createHabit(user.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Привычка не должна быть null.");

        boolean isDeleted = habitService.deleteHabit(user.getId(), habit.getId());
        assertTrue(isDeleted, "Привычка должна быть успешно удалена.");

        List<Habit> habits = habitService.getHabits(user.getId());
        assertTrue(habits.isEmpty(), "Список привычек должен быть пустым после удаления.");
    }

    @Test
    public void testGetHabitsByCreationDate() {
        // Получаем созданного пользователя
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть найден.");
        User user = userOptional.get();

        Habit habit1 = habitService.createHabit(user.getId(), "Exercise", "Morning exercise", 1);
        // Предполагается, что `createHabit` устанавливает дату создания как текущую дату
        Habit habit2 = habitService.createHabit(user.getId(), "Read", "Read a book", 2);

        LocalDate today = LocalDate.now();
        List<Habit> todayHabits = habitService.getHabitsByCreationDate(user.getId(), today);
        assertEquals(2, todayHabits.size(), "Должно быть 2 привычки, созданные сегодня.");

        LocalDate yesterday = today.minusDays(1);
        List<Habit> yesterdayHabits = habitService.getHabitsByCreationDate(user.getId(), yesterday);
        assertEquals(0, yesterdayHabits.size(), "Не должно быть привычек, созданных вчера.");
    }

    @Test
    public void testGetHabitsByFrequency() {
        // Получаем созданного пользователя
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть найден.");
        User user = userOptional.get();

        habitService.createHabit(user.getId(), "Exercise", "Morning exercise", 1); // Daily
        habitService.createHabit(user.getId(), "Read", "Read a book", 2); // Weekly

        List<Habit> dailyHabits = habitService.getHabitsByFrequency(user.getId(), 1);
        List<Habit> weeklyHabits = habitService.getHabitsByFrequency(user.getId(), 2);

        assertEquals(1, dailyHabits.size(), "Должна быть 1 ежедневная привычка.");
        assertEquals("Exercise", dailyHabits.get(0).getTitle(), "Название привычки должно совпадать.");

        assertEquals(1, weeklyHabits.size(), "Должна быть 1 еженедельная привычка.");
        assertEquals("Read", weeklyHabits.get(0).getTitle(), "Название привычки должно совпадать.");
    }

    @Test
    public void testUpdateNonexistentHabit() {
        // Получаем созданного пользователя
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть найден.");
        User user = userOptional.get();

        boolean isUpdated = habitService.updateHabit(user.getId(), 999, "Nonexistent", "Does not exist", 1);
        assertFalse(isUpdated, "Обновление несуществующей привычки должно вернуть false.");
    }

    @Test
    public void testDeleteNonexistentHabit() {
        // Получаем созданного пользователя
        Optional<User> userOptional = userService.loginUser("testuser@example.com", "password123");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть найден.");
        User user = userOptional.get();

        boolean isDeleted = habitService.deleteHabit(user.getId(), 999);
        assertFalse(isDeleted, "Удаление несуществующей привычки должно вернуть false.");
    }

}
