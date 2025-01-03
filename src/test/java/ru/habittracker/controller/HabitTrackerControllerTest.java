package ru.habittracker.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.habittracker.model.Habit;
import ru.habittracker.model.User;
import ru.habittracker.service.IHabitService;
import ru.habittracker.service.IHabitTrackerService;
import ru.habittracker.service.IUserService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для {@link ru.habittracker.controller.HabitTrackerController}.
 * <p>
 * Проверяет функциональность контроллера приложения.
 * </p>
 *
 * <p><strong>Автор:</strong> Ekaterina Ishchuk</p>
 */
class HabitTrackerControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private IHabitService habitService;

    @Mock
    private IHabitTrackerService habitTrackerService;

    @InjectMocks
    private HabitTrackerController habitTrackerController;

    private InputStream sysInBackup;

    private PrintStream originalOut;
    private PrintStream originalErr;

    /**
     * Инициализация моков и сохранение System.in перед каждым тестом.
     */
    @BeforeEach
    void setUp() throws UnsupportedEncodingException {
        originalOut = System.out;
        originalErr = System.err;

        System.setOut(new PrintStream(originalOut, true, "UTF-8"));
        System.setErr(new PrintStream(originalErr, true, "UTF-8"));

        MockitoAnnotations.openMocks(this);
        sysInBackup = System.in;

        habitTrackerController = new HabitTrackerController(userService, habitService, habitTrackerService);
    }

    /**
     * Восстановление System.in и сброс состояния контроллера после каждого теста.
     */
    @AfterEach
    void tearDown() {
        System.setIn(sysInBackup);
        habitTrackerController.setLoggedInUser(null);
    }

    /**
     * Тест успешной регистрации пользователя.
     */
    @Test
    @DisplayName("Тест успешной регистрации пользователя")
    void handleRegisterTest() {
        String simulatedInput = "user@example.com\npassword123\nJohn Doe\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        User mockUser = new User(1, "user@example.com", "password123", "John Doe");
        when(userService.registerUser(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(mockUser));

        habitTrackerController.handleRegister(new Scanner(System.in));

        verify(userService, times(1)).registerUser("user@example.com", "password123", "John Doe");
    }

    /**
     * Тест неудачной регистрации (email уже используется).
     */
    @Test
    @DisplayName("Тест неудачной регистрации пользователя (email уже используется)")
    void handleRegisterFailureTest() {
        String simulatedInput = "user@example.com\npassword123\nJohn Doe\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.registerUser(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        habitTrackerController.handleRegister(new Scanner(System.in));

        verify(userService, times(1)).registerUser("user@example.com", "password123", "John Doe");
    }

    /**
     * Тест успешного входа в систему.
     */
    @Test
    @DisplayName("Тест успешного входа в систему")
    void handleLoginTest() {
        String simulatedInput = "user@example.com\npassword123\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        User mockUser = new User(1, "user@example.com", "password123", "John Doe");
        when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.of(mockUser));

        habitTrackerController.handleLogin(new Scanner(System.in));

        verify(userService, times(1)).loginUser("user@example.com", "password123");
        Assertions.assertEquals(mockUser, habitTrackerController.getLoggedInUser());
    }

    /**
     * Тест неудачного входа в систему (неверный пароль).
     */
    @Test
    @DisplayName("Тест неудачного входа в систему (неверный пароль)")
    void handleLoginFailureTest() {
        String simulatedInput = "user@example.com\nwrongpassword\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.empty());

        habitTrackerController.handleLogin(new Scanner(System.in));

        verify(userService, times(1)).loginUser("user@example.com", "wrongpassword");
        Assertions.assertNull(habitTrackerController.getLoggedInUser());
    }

    /**
     * Тест успешного обновления профиля пользователя.
     */
    @Test
    @DisplayName("Тест успешного обновления профиля пользователя")
    void handleUpdateUserTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "newemail@example.com\nnewpassword\nNew Name\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.updateUser(anyInt(), anyString(), anyString(), anyString())).thenReturn(true);

        habitTrackerController.handleUpdateUser(new Scanner(System.in));

        verify(userService, times(1)).updateUser(1, "newemail@example.com", "newpassword", "New Name");
    }

    /**
     * Тест обновления профиля без входа в систему.
     */
    @Test
    @DisplayName("Тест обновления профиля без входа в систему")
    void handleUpdateUserNotLoggedInTest() {
        String simulatedInput = "newemail@example.com\nnewpassword\nNew Name\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleUpdateUser(new Scanner(System.in));

        verify(userService, never()).updateUser(anyInt(), anyString(), anyString(), anyString());
    }

    /**
     * Тест неудачного обновления профиля (email уже используется).
     */
    @Test
    @DisplayName("Тест неудачного обновления профиля (email уже используется)")
    void handleUpdateUserEmailAlreadyInUseTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "existing@example.com\nnewpassword\nNew Name\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.updateUser(anyInt(), anyString(), anyString(), anyString())).thenReturn(false);

        habitTrackerController.handleUpdateUser(new Scanner(System.in));

        verify(userService, times(1)).updateUser(1, "existing@example.com", "newpassword", "New Name");
    }

    /**
     * Тест успешного удаления пользователя.
     */
    @Test
    @DisplayName("Тест успешного удаления пользователя")
    void handleDeleteUserTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "yes\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.deleteUser(anyInt())).thenReturn(true);

        habitTrackerController.handleDeleteUser(new Scanner(System.in));

        verify(userService, times(1)).deleteUser(1);
        Assertions.assertNull(habitTrackerController.getLoggedInUser());
    }

    /**
     * Тест удаления пользователя без входа в систему.
     */
    @Test
    @DisplayName("Тест удаления пользователя без входа в систему")
    void handleDeleteUserNotLoggedInTest() {
        String simulatedInput = "yes\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleDeleteUser(new Scanner(System.in));

        verify(userService, never()).deleteUser(anyInt());
    }

    /**
     * Тест отмены удаления пользователя.
     */
    @Test
    @DisplayName("Тест отмены удаления пользователя")
    void handleDeleteUserCancellationTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "no\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleDeleteUser(new Scanner(System.in));

        verify(userService, never()).deleteUser(anyInt());
        Assertions.assertNotNull(habitTrackerController.getLoggedInUser());
    }

    /**
     * Тест успешного создания привычки.
     */
    @Test
    @DisplayName("Тест успешного создания привычки")
    void handleCreateHabitTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "Читать книгу\nЧитать 30 страниц ежедневно\n1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        Habit mockHabit = new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now());
        when(habitService.createHabit(anyInt(), anyString(), anyString(), anyInt())).thenReturn(mockHabit);

        habitTrackerController.handleCreateHabit(new Scanner(System.in));

        verify(habitService, times(1)).createHabit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1);
    }

    /**
     * Тест создания привычки без входа в систему.
     */
    @Test
    @DisplayName("Тест создания привычки без входа в систему")
    void handleCreateHabitNotLoggedInTest() {
        String simulatedInput = "Читать книгу\nЧитать 30 страниц ежедневно\n1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleCreateHabit(new Scanner(System.in));

        verify(habitService, never()).createHabit(anyInt(), anyString(), anyString(), anyInt());
    }

    /**
     * Тест создания привычки с некорректной частотой.
     */
    @Test
    @DisplayName("Тест создания привычки с некорректной частотой")
    void handleCreateHabitInvalidFrequencyTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "Читать книгу\nЧитать 30 страниц ежедневно\ninvalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleCreateHabit(new Scanner(System.in));

        verify(habitService, never()).createHabit(anyInt(), anyString(), anyString(), anyInt());
    }

    /**
     * Тест просмотра привычек пользователя.
     */
    @Test
    @DisplayName("Тест просмотра привычек пользователя")
    void handleViewHabitsTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        List<Habit> mockHabits = Arrays.asList(
                new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now()),
                new Habit(2, "Упражнения", "Утренняя зарядка", 2, 1, LocalDate.now())
        );
        when(habitService.getHabits(anyInt())).thenReturn(mockHabits);

        habitTrackerController.handleViewHabits();

        verify(habitService, times(1)).getHabits(1);
    }

    /**
     * Тест просмотра привычек без входа в систему.
     */
    @Test
    @DisplayName("Тест просмотра привычек без входа в систему")
    void handleViewHabitsNotLoggedInTest() {
        habitTrackerController.handleViewHabits();

        verify(habitService, never()).getHabits(anyInt());
    }

    /**
     * Тест просмотра привычек по дате создания.
     */
    @Test
    @DisplayName("Тест просмотра привычек по дате создания")
    void handleViewHabitsByDateTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = LocalDate.now().toString() + "\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        List<Habit> mockHabits = Collections.singletonList(
                new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now())
        );
        when(habitService.getHabitsByCreationDate(anyInt(), any(LocalDate.class))).thenReturn(mockHabits);

        habitTrackerController.handleViewHabitsByDate(new Scanner(System.in));

        verify(habitService, times(1)).getHabitsByCreationDate(1, LocalDate.now());
    }

    /**
     * Тест просмотра привычек по дате с некорректным вводом.
     */
    @Test
    @DisplayName("Тест просмотра привычек по дате с некорректным вводом")
    void handleViewHabitsByDateInvalidDateTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid-date\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleViewHabitsByDate(new Scanner(System.in));

        verify(habitService, never()).getHabitsByCreationDate(anyInt(), any(LocalDate.class));
    }

    /**
     * Тест просмотра привычек по частоте.
     */
    @Test
    @DisplayName("Тест просмотра привычек по частоте")
    void handleViewHabitsByStatusTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        List<Habit> mockHabits = Collections.singletonList(
                new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now())
        );
        when(habitService.getHabitsByFrequency(anyInt(), anyInt())).thenReturn(mockHabits);

        habitTrackerController.handleViewHabitsByStatus(new Scanner(System.in));

        verify(habitService, times(1)).getHabitsByFrequency(1, 1);
    }

    /**
     * Тест просмотра привычек по частоте с некорректным вводом.
     */
    @Test
    @DisplayName("Тест просмотра привычек по частоте с некорректным вводом")
    void handleViewHabitsByStatusInvalidStatusTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleViewHabitsByStatus(new Scanner(System.in));

        verify(habitService, never()).getHabitsByFrequency(anyInt(), anyInt());
    }

    /**
     * Тест успешного обновления привычки.
     */
    @Test
    @DisplayName("Тест успешного обновления привычки")
    void handleUpdateHabitTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\nНовое название\nНовое описание\n1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(habitService.updateHabit(anyInt(), anyInt(), anyString(), anyString(), anyInt())).thenReturn(true);

        habitTrackerController.handleUpdateHabit(new Scanner(System.in));

        verify(habitService, times(1)).updateHabit(1, 1, "Новое название", "Новое описание", 1);
    }

    /**
     * Тест обновления привычки с некорректным вводом.
     */
    @Test
    @DisplayName("Тест обновления привычки с некорректным вводом")
    void handleUpdateHabitInvalidInputTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid\nНовое название\nНовое описание\ninvalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleUpdateHabit(new Scanner(System.in));

        verify(habitService, never()).updateHabit(anyInt(), anyInt(), anyString(), anyString(), anyInt());
    }

    /**
     * Тест успешного удаления привычки.
     */
    @Test
    @DisplayName("Тест успешного удаления привычки")
    void handleDeleteHabitTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(habitService.deleteHabit(anyInt(), anyInt())).thenReturn(true);

        habitTrackerController.handleDeleteHabit(new Scanner(System.in));

        verify(habitService, times(1)).deleteHabit(1, 1);
    }

    /**
     * Тест удаления привычки с некорректным вводом.
     */
    @Test
    @DisplayName("Тест удаления привычки с некорректным вводом")
    void handleDeleteHabitInvalidInputTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleDeleteHabit(new Scanner(System.in));

        verify(habitService, never()).deleteHabit(anyInt(), anyInt());
    }

    /**
     * Тест успешной отметки привычки как выполненной.
     */
    @Test
    @DisplayName("Тест успешной отметки привычки как выполненной")
    void handleMarkCompleteTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        doNothing().when(habitTrackerService).markHabitCompletion(anyInt(), anyInt(), any(LocalDate.class));

        habitTrackerController.handleMarkComplete(new Scanner(System.in));

        verify(habitTrackerService, times(1)).markHabitCompletion(1, 1, LocalDate.now());
    }

    /**
     * Тест отметки привычки как выполненной с некорректным вводом.
     */
    @Test
    @DisplayName("Тест отметки привычки как выполненной с некорректным вводом")
    void handleMarkCompleteInvalidInputTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerController.handleMarkComplete(new Scanner(System.in));

        verify(habitTrackerService, never()).markHabitCompletion(anyInt(), anyInt(), any(LocalDate.class));
    }

    /**
     * Тест просмотра истории выполнения привычки.
     */
    @Test
    @DisplayName("Тест просмотра истории выполнения привычки")
    void handleViewHabitHistoryTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(habitTrackerService.getHabitHistory(anyInt(), anyInt())).thenReturn("История привычки");

        habitTrackerController.handleViewHabitHistory(new Scanner(System.in));

        verify(habitTrackerService, times(1)).getHabitHistory(1, 1);
    }

    /**
     * Тест просмотра статистики выполнения привычки.
     */
    @Test
    @DisplayName("Тест просмотра статистики выполнения привычки")
    void handleViewHabitStatisticsTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(habitTrackerService.calculateStreak(anyInt(), anyInt())).thenReturn(5);
        when(habitTrackerService.calculateSuccessRate(anyInt(), anyInt())).thenReturn(80.0);

        habitTrackerController.handleViewHabitStatistics(new Scanner(System.in));

        verify(habitTrackerService, times(1)).calculateStreak(1, 1);
        verify(habitTrackerService, times(1)).calculateSuccessRate(1, 1);
    }

    /**
     * Тест генерации отчёта по прогрессу.
     */
    @Test
    @DisplayName("Тест генерации отчёта по прогрессу")
    void handleGenerateReportTest() {
        habitTrackerController.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        List<Habit> mockHabits = Arrays.asList(
                new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now()),
                new Habit(2, "Упражнения", "Утренняя зарядка", 1, 1, LocalDate.now())
        );
        when(habitService.getHabits(anyInt())).thenReturn(mockHabits);
        when(habitTrackerService.generateProgressReport(anyInt(), anyList())).thenReturn("Отчет о прогрессе");

        habitTrackerController.handleGenerateReport();

        verify(habitService, times(1)).getHabits(1);
        verify(habitTrackerService, times(1)).generateProgressReport(1, mockHabits);
    }

    /**
     * Тест генерации отчёта без входа в систему.
     */
    @Test
    @DisplayName("Тест генерации отчёта без входа в систему")
    void handleGenerateReportNotLoggedInTest() {
        habitTrackerController.handleGenerateReport();

        verify(habitService, never()).getHabits(anyInt());
        verify(habitTrackerService, never()).generateProgressReport(anyInt(), anyList());
    }
}
