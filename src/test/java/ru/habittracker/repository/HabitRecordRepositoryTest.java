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
import ru.habittracker.model.Habit;
import ru.habittracker.model.HabitRecord;
import ru.habittracker.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class HabitRecordRepositoryTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password")
            .withInitScript("init.sql");

    private static DatabaseConnectionManager dbManager;
    private static HabitRecordRepository habitRecordRepository;
    private static HabitRepository habitRepository;
    private static UserRepository userRepository;
    private static User testUser;
    private static Habit testHabit;

    @BeforeAll
    public static void globalSetUp() throws SQLException, LiquibaseException {
        // Инициализация DatabaseConnectionManager
        dbManager = new DatabaseConnectionManager(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getDriverClassName()
        );

        // Настройка базы данных и применение Liquibase changelog
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

        // Инициализация репозиториев
        habitRecordRepository = new HabitRecordRepository(dbManager);
        habitRepository = new HabitRepository(dbManager);
        userRepository = new UserRepository(dbManager);
    }

    @BeforeEach
    public void setUp() throws SQLException {
        // Очистка базы данных перед каждым тестом и создание тестового пользователя и привычки
        try (Connection connection = dbManager.getConnection()) {
            connection.createStatement().execute(
                    "TRUNCATE TABLE service.habit_records, service.habits, service.users RESTART IDENTITY CASCADE;"
            );
        }

        // Создание тестового пользователя
        User user = new User(0, "user@example.com", "password123", "Test User");
        Optional<User> savedUserOptional = userRepository.save(user);
        assertTrue(savedUserOptional.isPresent(), "Пользователь должен быть успешно сохранен.");
        testUser = savedUserOptional.get();

        // Создание тестовой привычки
        Habit habit = new Habit(0, "Exercise", "Morning exercise", 1, testUser.getId(), LocalDate.now());
        testHabit = habitRepository.save(habit);
        assertNotNull(testHabit, "Привычка должна быть успешно сохранена.");
    }

    @Test
    public void testSaveHabitRecord() {
        HabitRecord record = new HabitRecord(testHabit.getId(), LocalDate.now(), true);
        Optional<HabitRecord> savedRecordOptional = habitRecordRepository.save(record);

        assertTrue(savedRecordOptional.isPresent(), "Запись привычки должна быть успешно сохранена.");
        HabitRecord savedRecord = savedRecordOptional.get();
        assertEquals(testHabit.getId(), savedRecord.getHabitId(), "ID привычки должен совпадать.");
        assertEquals(LocalDate.now(), savedRecord.getDate(), "Дата должна совпадать.");
        assertTrue(savedRecord.isCompleted(), "Поле 'completed' должно быть true.");
    }

    @Test
    public void testFindByHabitId() {
        HabitRecord record1 = new HabitRecord(testHabit.getId(), LocalDate.now(), true);
        HabitRecord record2 = new HabitRecord(testHabit.getId(), LocalDate.now().minusDays(1), false);
        habitRecordRepository.save(record1);
        habitRecordRepository.save(record2);

        List<HabitRecord> records = habitRecordRepository.findByHabitId(testHabit.getId());
        assertEquals(2, records.size(), "Должно быть 2 записи для данной привычки.");
    }

    @Test
    public void testFindByUserIdAndDate() {
        HabitRecord record = new HabitRecord(testHabit.getId(), LocalDate.now(), true);
        habitRecordRepository.save(record);

        List<HabitRecord> records = habitRecordRepository.findByUserIdAndDate(testUser.getId(), LocalDate.now());
        assertEquals(1, records.size(), "Должна быть 1 запись для пользователя на заданную дату.");
    }

    /*@Test
    public void testDeleteHabitRecord() {
        HabitRecord record = new HabitRecord(testHabit.getId(), LocalDate.now(), true);
        Optional<HabitRecord> savedRecordOptional = habitRecordRepository.save(record);
        assertTrue(savedRecordOptional.isPresent(), "Запись привычки должна быть успешно сохранена.");

        HabitRecord savedRecord = savedRecordOptional.get();

        boolean isDeleted = habitRecordRepository.deleteByHabitIdAndDate(savedRecord.getHabitId(), savedRecord.getDate());
        assertTrue(isDeleted, "Запись привычки должна быть успешно удалена.");

        // Проверяем, что запись действительно удалена
        List<HabitRecord> records = habitRecordRepository.findByHabitId(savedRecord.getHabitId());
        boolean recordExists = records.stream().anyMatch(r -> r.getDate().equals(savedRecord.getDate()));
        assertFalse(recordExists, "Запись привычки не должна существовать после удаления.");
    }*/
}
