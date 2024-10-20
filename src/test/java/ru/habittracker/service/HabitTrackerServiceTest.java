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
import ru.habittracker.service.interfaces.IHabitTrackerService;
import ru.habittracker.service.interfaces.IUserService;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link ru.habittracker.service.HabitTrackerService}.
 * <p>
 * Проверяет корректность логики отслеживания выполнения привычек.
 * </p>
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public class HabitTrackerServiceTest extends BaseHabitTest {

    private static DatabaseConnectionManager dbManager;
    private static IHabitTrackerService habitTrackerService;
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

        habitTrackerService = new HabitTrackerService(dbManager);
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
        Optional<User> userOptional = userService.registerUser("trackeruser@example.com", "password123", "Tracker User");
        assertTrue(userOptional.isPresent(), "User should be successfully created.");
        testUser = userOptional.get();
    }

    /**
     * Тест отметки привычки как выполненной.
     */
    @Test
    public void testMarkHabitCompletion() {
        Habit habit = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Habit should not be null.");

        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now());

        String history = habitTrackerService.getHabitHistory(testUser.getId(), habit.getId());
        assertTrue(history.contains(LocalDate.now().toString()), "History should contain today's date.");
    }

    /**
     * Тест вычисления текущей серии выполнения привычки.
     */
    @Test
    public void testCalculateStreak() {
        Habit habit = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Habit should not be null.");

        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now().minusDays(2));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now().minusDays(1));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now());

        int streak = habitTrackerService.calculateStreak(testUser.getId(), habit.getId());
        assertEquals(3, streak, "Streak should be 3.");
    }

    /**
     * Тест вычисления процента успешного выполнения привычки.
     */
    @Test
    public void testCalculateSuccessRate() {
        Habit habit = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Habit should not be null.");

        LocalDate startDate = LocalDate.now().minusDays(30);
        for (int i = 0; i < 30; i++) {
            if (i % 2 == 0) {
                habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), startDate.plusDays(i));
            }
        }

        double successRate = habitTrackerService.calculateSuccessRate(testUser.getId(), habit.getId());
        assertEquals(50.0, successRate, 0.1, "Success rate should be approximately 50%.");
    }

    /**
     * Тест генерации отчёта по прогрессу.
     */
    @Test
    public void testGenerateProgressReport() {
        Habit habit1 = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        Habit habit2 = habitService.createHabit(testUser.getId(), "Read", "Read a book", 1);

        habitTrackerService.markHabitCompletion(testUser.getId(), habit1.getId(), LocalDate.now().minusDays(1));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit1.getId(), LocalDate.now());

        habitTrackerService.markHabitCompletion(testUser.getId(), habit2.getId(), LocalDate.now().minusDays(2));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit2.getId(), LocalDate.now().minusDays(1));

        List<Habit> habits = habitService.getHabits(testUser.getId());
        String report = habitTrackerService.generateProgressReport(testUser.getId(), habits);

        assertNotNull(report, "Report should not be null.");
        assertTrue(report.contains("Exercise"), "Report should contain habit 'Exercise'.");
        assertTrue(report.contains("Read"), "Report should contain habit 'Read'.");
    }

    /**
     * Тест получения истории выполнения привычки.
     */
    @Test
    public void testGetHabitHistory() {
        Habit habit = habitService.createHabit(testUser.getId(), "Meditate", "Evening meditation", 1);

        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now().minusDays(2));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now().minusDays(1));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now());

        String history = habitTrackerService.getHabitHistory(testUser.getId(), habit.getId());
        assertTrue(history.contains(LocalDate.now().toString()), "History should contain today's date.");
        assertTrue(history.contains(LocalDate.now().minusDays(1).toString()), "History should contain yesterday's date.");
        assertTrue(history.contains(LocalDate.now().minusDays(2).toString()), "History should contain the date two days ago.");
    }
}
