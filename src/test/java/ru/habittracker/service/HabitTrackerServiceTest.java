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

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class HabitTrackerServiceTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password")
            .withInitScript("init.sql");

    private static DatabaseConnectionManager dbManager;
    private static HabitTrackerService habitTrackerService;
    private static HabitService habitService;
    private static UserService userService;
    private static User testUser;

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

        habitTrackerService = new HabitTrackerService(dbManager);
        habitService = new HabitService(dbManager);
        userService = new UserService(dbManager);
    }

    @BeforeEach
    public void setUp() throws SQLException {
        Optional<User> userOptional = userService.registerUser("trackeruser@example.com", "password123", "Tracker User");
        assertTrue(userOptional.isPresent(), "Пользователь должен быть успешно создан.");
        testUser = userOptional.get();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection connection = dbManager.getConnection()) {
            connection.createStatement().execute(
                    "TRUNCATE TABLE service.habit_records, service.habits, service.users RESTART IDENTITY CASCADE;"
            );
        }
    }

    @Test
    public void testMarkHabitCompletion() {
        Habit habit = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Привычка не должна быть null.");

        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now());

        String history = habitTrackerService.getHabitHistory(testUser.getId(), habit.getId());
        assertTrue(history.contains(LocalDate.now().toString()), "История должна содержать сегодняшнюю дату.");
    }

    @Test
    public void testCalculateStreak() {
        Habit habit = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Привычка не должна быть null.");

        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now().minusDays(2));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now().minusDays(1));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now());

        int streak = habitTrackerService.calculateStreak(testUser.getId(), habit.getId());
        assertEquals(3, streak, "Серия должна быть равна 3.");
    }

    @Test
    public void testCalculateSuccessRate() {
        Habit habit = habitService.createHabit(testUser.getId(), "Exercise", "Morning exercise", 1);
        assertNotNull(habit, "Привычка не должна быть null.");

        LocalDate startDate = LocalDate.now().minusDays(30);
        for (int i = 0; i < 30; i++) {
            if (i % 2 == 0) {
                habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), startDate.plusDays(i));
            }
        }

        double successRate = habitTrackerService.calculateSuccessRate(testUser.getId(), habit.getId());
        assertEquals(50.0, successRate, 0.1, "Процент успеха должен быть примерно 50%.");
    }

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

        assertNotNull(report, "Отчет не должен быть null.");
        assertTrue(report.contains("Exercise"), "Отчет должен содержать привычку 'Exercise'.");
        assertTrue(report.contains("Read"), "Отчет должен содержать привычку 'Read'.");
    }

    @Test
    public void testGetHabitHistory() {
        Habit habit = habitService.createHabit(testUser.getId(), "Meditate", "Evening meditation", 1);

        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now().minusDays(2));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now().minusDays(1));
        habitTrackerService.markHabitCompletion(testUser.getId(), habit.getId(), LocalDate.now());

        String history = habitTrackerService.getHabitHistory(testUser.getId(), habit.getId());
        assertTrue(history.contains(LocalDate.now().toString()), "История должна содержать сегодняшнюю дату.");
        assertTrue(history.contains(LocalDate.now().minusDays(1).toString()), "История должна содержать дату вчерашнего дня.");
        assertTrue(history.contains(LocalDate.now().minusDays(2).toString()), "История должна содержать дату позавчерашнего дня.");
    }
}
