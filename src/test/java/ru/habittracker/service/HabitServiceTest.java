package ru.habittracker.service;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.*;
import ru.habittracker.BaseHabitTest;
import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.Habit;
import ru.habittracker.model.User;
import ru.habittracker.service.interfaces.IHabitService;
import ru.habittracker.service.interfaces.IUserService;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link ru.habittracker.service.HabitService}.
 * <p>
 * Проверяет корректность логики управления привычками.
 * </p>
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public class HabitServiceTest extends BaseHabitTest {

    private static DatabaseConnectionManager dbManager;
    private static IHabitService habitService;
    private static IUserService userService;
    private static User testUser;

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

        habitService = new HabitService(dbManager);
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
                    "TRUNCATE TABLE service.habit_records, service.habits, service.users RESTART IDENTITY CASCADE;"
            );
        }

        // Создание тестового пользователя
        Optional<User> userOptional = userService.registerUser("testuser@example.com", "password123", "Test User");
        assertTrue(userOptional.isPresent(), "User should be successfully created.");
        testUser = userOptional.get();
    }

    /**
     * Тест создания привычки.
     */
    @Test
    public void testCreateHabit() {
        Habit habit = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Habit should not be null.");
        assertTrue(habit.getId() > 0, "Habit ID should be greater than 0.");
        assertEquals("Exercise", habit.getTitle(), "Habit title should match.");
    }

    /**
     * Тест получения всех привычек пользователя.
     */
    @Test
    public void testGetHabits() {
        habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        habitService.createHabit(testUser.getId(), "Read", "Read a book", 2);

        List<Habit> habits = habitService.getHabits(testUser.getId());
        assertEquals(2, habits.size(), "There should be 2 habits created.");
    }

    /**
     * Тест обновления привычки.
     */
    @Test
    public void testUpdateHabit() {
        Habit habit = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Habit should not be null.");

        boolean isUpdated = habitService.updateHabit(testUser.getId(), habit.getId(), "Yoga", "Evening yoga", 2);
        assertTrue(isUpdated, "Habit should be successfully updated.");

        List<Habit> habits = habitService.getHabits(testUser.getId());
        Habit updatedHabit = habits.stream().filter(h -> h.getId() == habit.getId()).findFirst().orElse(null);
        assertNotNull(updatedHabit, "Updated habit should exist.");
        assertEquals("Yoga", updatedHabit.getTitle(), "Habit title should be updated.");
        assertEquals("Evening yoga", updatedHabit.getDescription(), "Habit description should be updated.");
        assertEquals(2, updatedHabit.getFrequency(), "Habit frequency should be updated.");
    }

    /**
     * Тест удаления привычки.
     */
    @Test
    public void testDeleteHabit() {
        Habit habit = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Habit should not be null.");

        boolean isDeleted = habitService.deleteHabit(testUser.getId(), habit.getId());
        assertTrue(isDeleted, "Habit should be successfully deleted.");

        List<Habit> habits = habitService.getHabits(testUser.getId());
        assertTrue(habits.isEmpty(), "Habit list should be empty after deletion.");
    }

    /**
     * Тест получения привычек по дате создания.
     */
    @Test
    public void testGetHabitsByCreationDate() {
        habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        habitService.createHabit(testUser.getId(), "Read", "Read a book", 2);

        LocalDate today = LocalDate.now();
        List<Habit> todayHabits = habitService.getHabitsByCreationDate(testUser.getId(), today);
        assertEquals(2, todayHabits.size(), "There should be 2 habits created today.");

        LocalDate yesterday = today.minusDays(1);
        List<Habit> yesterdayHabits = habitService.getHabitsByCreationDate(testUser.getId(), yesterday);
        assertEquals(0, yesterdayHabits.size(), "There should be no habits created yesterday.");
    }

    /**
     * Тест получения привычек по частоте.
     */
    @Test
    public void testGetHabitsByFrequency() {
        habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1); // Ежедневная
        habitService.createHabit(testUser.getId(), "Read", "Read a book", 2); // Недельная

        List<Habit> dailyHabits = habitService.getHabitsByFrequency(testUser.getId(), 1);
        List<Habit> weeklyHabits = habitService.getHabitsByFrequency(testUser.getId(), 2);

        assertEquals(1, dailyHabits.size(), "There should be 1 daily habit.");
        assertEquals("Exercise", dailyHabits.get(0).getTitle(), "Habit title should match.");

        assertEquals(1, weeklyHabits.size(), "There should be 1 weekly habit.");
        assertEquals("Read", weeklyHabits.get(0).getTitle(), "Habit title should match.");
    }

    /**
     * Тест обновления несуществующей привычки.
     */
    @Test
    public void testUpdateNonexistentHabit() {
        boolean isUpdated = habitService.updateHabit(testUser.getId(), 999, "Nonexistent", "Does not exist", 1);
        assertFalse(isUpdated, "Updating a nonexistent habit should return false.");
    }

    /**
     * Тест удаления несуществующей привычки.
     */
    @Test
    public void testDeleteNonexistentHabit() {
        boolean isDeleted = habitService.deleteHabit(testUser.getId(), 999);
        assertFalse(isDeleted, "Deleting a nonexistent habit should return false.");
    }
}
