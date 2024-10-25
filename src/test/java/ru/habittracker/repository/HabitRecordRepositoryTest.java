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
import ru.habittracker.model.HabitRecord;
import ru.habittracker.model.User;
import ru.habittracker.repository.impl.HabitRecordRepository;
import ru.habittracker.repository.impl.HabitRepository;
import ru.habittracker.repository.impl.UserRepository;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для {@link HabitRecordRepository}.
 * <p>
 * Проверяет корректность операций с записями о выполнении привычек.
 * </p>
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
public class HabitRecordRepositoryTest extends BaseHabitTest {

    private static DatabaseConnectionManager dbManager;
    private static IHabitRecordRepository habitRecordRepository;
    private static IHabitRepository habitRepository;
    private static IUserRepository userRepository;
    private static User testUser;
    private static Habit testHabit;

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

        habitRecordRepository = new HabitRecordRepository(dbManager);
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
                    "TRUNCATE TABLE service.habit_records, service.habits, service.users RESTART IDENTITY CASCADE;"
            );
        }

        // Создание тестового пользователя
        User user = new User(0, "user@example.com", "password123", "Test User");
        Optional<User> savedUserOptional = userRepository.save(user);
        assertTrue(savedUserOptional.isPresent(), "User should be successfully saved.");
        testUser = savedUserOptional.get();

        // Создание тестовой привычки
        Habit habit = new Habit(0, "Exercise", "Morning exercise", 1, testUser.getId(), LocalDate.now());
        testHabit = habitRepository.save(habit);
        assertNotNull(testHabit, "Habit should be successfully saved.");
    }

    /**
     * Тест сохранения записи о выполнении привычки.
     */
    @Test
    @DisplayName("Тест сохранения записи о выполнении привычки")
    public void testSaveHabitRecord() {
        HabitRecord record = new HabitRecord(testHabit.getId(), LocalDate.now(), true);
        Optional<HabitRecord> savedRecordOptional = habitRecordRepository.save(record);

        assertTrue(savedRecordOptional.isPresent(), "Habit record should be successfully saved.");
        HabitRecord savedRecord = savedRecordOptional.get();
        assertEquals(testHabit.getId(), savedRecord.getHabitId(), "Habit ID should match.");
        assertEquals(LocalDate.now(), savedRecord.getDate(), "Date should match.");
        assertTrue(savedRecord.isCompleted(), "'completed' field should be true.");
    }

    /**
     * Тест поиска записей по ID привычки.
     */
    @Test
    @DisplayName("Тест поиска записей по ID привычки")
    public void testFindByHabitId() {
        HabitRecord record1 = new HabitRecord(testHabit.getId(), LocalDate.now(), true);
        HabitRecord record2 = new HabitRecord(testHabit.getId(), LocalDate.now().minusDays(1), false);
        habitRecordRepository.save(record1);
        habitRecordRepository.save(record2);

        List<HabitRecord> records = habitRecordRepository.findByHabitId(testHabit.getId());
        assertEquals(2, records.size(), "There should be 2 records for the given habit.");
    }

    /**
     * Тест поиска записей по ID пользователя и дате.
     */
    @Test
    @DisplayName("Тест поиска записей по ID пользователя и дате")
    public void testFindByUserIdAndDate() {
        HabitRecord record = new HabitRecord(testHabit.getId(), LocalDate.now(), true);
        habitRecordRepository.save(record);

        List<HabitRecord> records = habitRecordRepository.findByUserIdAndDate(testUser.getId(), LocalDate.now());
        assertEquals(1, records.size(), "There should be 1 record for the user on the given date.");
    }

    /**
     * Тест поиска записи по её ID.
     */
    @Test
    @DisplayName("Тест поиска записи по её ID")
    public void testFindById() {
        HabitRecord record = new HabitRecord(testHabit.getId(), LocalDate.now(), true);
        Optional<HabitRecord> savedRecordOptional = habitRecordRepository.save(record);
        assertTrue(savedRecordOptional.isPresent(), "Record should be saved.");

        int recordId = savedRecordOptional.get().getId();

        Optional<HabitRecord> foundRecordOptional = habitRecordRepository.findById(recordId);
        assertTrue(foundRecordOptional.isPresent(), "Record should be found by ID.");
        HabitRecord foundRecord = foundRecordOptional.get();
        assertEquals(recordId, foundRecord.getId(), "Record IDs should match.");
    }

    /**
     * Тест удаления записи о выполнении привычки.
     */
    @Test
    @DisplayName("Тест удаления записи о выполнении привычки")
    public void testDeleteHabitRecord() {
        HabitRecord record = new HabitRecord(testHabit.getId(), LocalDate.now(), true);
        Optional<HabitRecord> savedRecordOptional = habitRecordRepository.save(record);
        assertTrue(savedRecordOptional.isPresent(), "Habit record should be successfully saved.");

        int recordId = savedRecordOptional.get().getId();

        boolean isDeleted = habitRecordRepository.delete(recordId);
        assertTrue(isDeleted, "Habit record should be successfully deleted.");

        // Проверка отсутствия записи после удаления
        Optional<HabitRecord> foundRecord = habitRecordRepository.findById(recordId);
        assertFalse(foundRecord.isPresent(), "Record should not exist after deletion.");
    }
}
