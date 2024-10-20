package ru.habittracker.repository;

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
import ru.habittracker.repository.interfaces.IHabitRepository;
import ru.habittracker.repository.interfaces.IUserRepository;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link ru.habittracker.repository.HabitRepository}.
 * <p>
 * Проверяет корректность операций с привычками в базе данных.
 * </p>
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public class HabitRepositoryTest extends BaseHabitTest {

    private static DatabaseConnectionManager dbManager;
    private static IHabitRepository habitRepository;
    private static IUserRepository userRepository;
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

        habitRepository = new HabitRepository(dbManager);
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
                    "TRUNCATE TABLE service.habits, service.users RESTART IDENTITY CASCADE;"
            );
        }

        // Создание тестового пользователя
        User user = new User(0, "user@example.com", "password123", "Test User");
        Optional<User> savedUserOptional = userRepository.save(user);
        assertTrue(savedUserOptional.isPresent(), "User should be successfully saved.");
        testUser = savedUserOptional.get();
    }

    /**
     * Тест сохранения привычки.
     */
    @Test
    public void testSaveHabit() {
        Habit habit = new Habit(0, "Exercise", "Morning exercise", 1, testUser.getId(), LocalDate.now());
        Habit savedHabit = habitRepository.save(habit);

        assertNotNull(savedHabit, "Habit should be saved successfully.");
        assertTrue(savedHabit.getId() > 0, "Habit ID should be greater than 0.");
        assertEquals("Exercise", savedHabit.getTitle(), "Titles should match.");
    }

    /**
     * Тест поиска привычки по ID и ID пользователя.
     */
    @Test
    public void testFindByIdAndUserId() {
        Habit habit = new Habit(0, "Exercise", "Morning exercise", 1, testUser.getId(), LocalDate.now());
        Habit savedHabit = habitRepository.save(habit);

        Habit foundHabit = habitRepository.findByIdAndUserId(savedHabit.getId(), testUser.getId());
        assertNotNull(foundHabit, "Habit should be found by ID and User ID.");
        assertEquals(savedHabit.getId(), foundHabit.getId(), "Habit IDs should match.");
    }

    /**
     * Тест поиска всех привычек пользователя.
     */
    @Test
    public void testFindByUserId() {
        Habit habit1 = new Habit(0, "Exercise", "Morning exercise", 1, testUser.getId(), LocalDate.now());
        Habit habit2 = new Habit(0, "Read", "Read a book", 2, testUser.getId(), LocalDate.now());
        habitRepository.save(habit1);
        habitRepository.save(habit2);

        List<Habit> habits = habitRepository.findByUserId(testUser.getId());
        assertEquals(2, habits.size(), "There should be 2 habits for the user.");
    }

    /**
     * Тест обновления привычки.
     */
    @Test
    public void testUpdateHabit() {
        Habit habit = new Habit(0, "Exercise", "Morning exercise", 1, testUser.getId(), LocalDate.now());
        Habit savedHabit = habitRepository.save(habit);

        savedHabit.setTitle("Updated Exercise");
        savedHabit.setDescription("Updated description");
        savedHabit.setFrequency(2);

        boolean isUpdated = habitRepository.update(savedHabit);
        assertTrue(isUpdated, "Habit should be updated successfully.");

        Habit updatedHabit = habitRepository.findByIdAndUserId(savedHabit.getId(), testUser.getId());
        assertEquals("Updated Exercise", updatedHabit.getTitle(), "Title should be updated.");
        assertEquals("Updated description", updatedHabit.getDescription(), "Description should be updated.");
        assertEquals(2, updatedHabit.getFrequency(), "Frequency should be updated.");
    }

    /**
     * Тест удаления привычки по ID и ID пользователя.
     */
    @Test
    public void testDeleteByIdAndUserId() {
        Habit habit = new Habit(0, "Exercise", "Morning exercise", 1, testUser.getId(), LocalDate.now());
        Habit savedHabit = habitRepository.save(habit);

        boolean isDeleted = habitRepository.deleteByIdAndUserId(savedHabit.getId(), testUser.getId());
        assertTrue(isDeleted, "Habit should be deleted successfully.");

        Habit deletedHabit = habitRepository.findByIdAndUserId(savedHabit.getId(), testUser.getId());
        assertNull(deletedHabit, "Habit should no longer exist.");
    }
}
