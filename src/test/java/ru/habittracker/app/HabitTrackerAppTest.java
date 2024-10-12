package ru.habittracker.app;

import org.junit.jupiter.api.*;
import org.mockito.*;
import ru.habittracker.model.Habit;
import ru.habittracker.model.User;
import ru.habittracker.service.HabitService;
import ru.habittracker.service.HabitTrackerService;
import ru.habittracker.service.UserService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HabitTrackerAppTest {

    @Mock
    private UserService userService;

    @Mock
    private HabitService habitService;

    @Mock
    private HabitTrackerService habitTrackerService;

    @InjectMocks
    private HabitTrackerApp habitTrackerApp;

    private InputStream sysInBackup;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sysInBackup = System.in; // Сохраняем текущий System.in для последующего восстановления

        habitTrackerApp = new HabitTrackerApp(userService, habitService, habitTrackerService);
    }

    @AfterEach
    void tearDown() {
        System.setIn(sysInBackup); // Восстанавливаем System.in
        habitTrackerApp.setLoggedInUser(null); // Сбрасываем состояние входа через сеттер
    }

    @Test
    void handleRegisterTest() {
        String simulatedInput = "user@example.com\npassword123\nJohn Doe\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        User mockUser = new User(1, "user@example.com", "password123", "John Doe");
        when(userService.registerUser(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(mockUser));

        habitTrackerApp.handleRegister(new Scanner(System.in));

        verify(userService, times(1)).registerUser("user@example.com", "password123", "John Doe");
    }

    @Test
    void handleRegisterFailureTest() {
        String simulatedInput = "user@example.com\npassword123\nJohn Doe\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.registerUser(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        habitTrackerApp.handleRegister(new Scanner(System.in));

        verify(userService, times(1)).registerUser("user@example.com", "password123", "John Doe");
    }

    @Test
    void handleLoginTest() {
        String simulatedInput = "user@example.com\npassword123\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        User mockUser = new User(1, "user@example.com", "password123", "John Doe");
        when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.of(mockUser));

        habitTrackerApp.handleLogin(new Scanner(System.in));

        verify(userService, times(1)).loginUser("user@example.com", "password123");
        Assertions.assertEquals(mockUser, habitTrackerApp.getLoggedInUser());
    }

    @Test
    void handleLoginFailureTest() {
        String simulatedInput = "user@example.com\nwrongpassword\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.loginUser(anyString(), anyString())).thenReturn(Optional.empty());

        habitTrackerApp.handleLogin(new Scanner(System.in));

        verify(userService, times(1)).loginUser("user@example.com", "wrongpassword");
        Assertions.assertNull(habitTrackerApp.getLoggedInUser());
    }

    @Test
    void handleUpdateUserTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "newemail@example.com\nnewpassword\nNew Name\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.updateUser(anyInt(), anyString(), anyString(), anyString())).thenReturn(true);

        habitTrackerApp.handleUpdateUser(new Scanner(System.in));

        verify(userService, times(1)).updateUser(1, "newemail@example.com", "newpassword", "New Name");
    }

    @Test
    void handleUpdateUserNotLoggedInTest() {
        String simulatedInput = "newemail@example.com\nnewpassword\nNew Name\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleUpdateUser(new Scanner(System.in));

        verify(userService, never()).updateUser(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    void handleUpdateUserEmailAlreadyInUseTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "existing@example.com\nnewpassword\nNew Name\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.updateUser(anyInt(), anyString(), anyString(), anyString())).thenReturn(false);

        habitTrackerApp.handleUpdateUser(new Scanner(System.in));

        verify(userService, times(1)).updateUser(1, "existing@example.com", "newpassword", "New Name");
    }

    @Test
    void handleDeleteUserTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "yes\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(userService.deleteUser(anyInt())).thenReturn(true);

        habitTrackerApp.handleDeleteUser(new Scanner(System.in));

        verify(userService, times(1)).deleteUser(1);
        Assertions.assertNull(habitTrackerApp.getLoggedInUser());
    }

    @Test
    void handleDeleteUserNotLoggedInTest() {
        String simulatedInput = "yes\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleDeleteUser(new Scanner(System.in));

        verify(userService, never()).deleteUser(anyInt());
    }

    @Test
    void handleDeleteUserCancellationTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "no\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleDeleteUser(new Scanner(System.in));

        verify(userService, never()).deleteUser(anyInt());
        Assertions.assertNotNull(habitTrackerApp.getLoggedInUser());
    }

    @Test
    void handleCreateHabitTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "Читать книгу\nЧитать 30 страниц ежедневно\n1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        Habit mockHabit = new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now());
        when(habitService.createHabit(anyInt(), anyString(), anyString(), anyInt())).thenReturn(mockHabit);

        habitTrackerApp.handleCreateHabit(new Scanner(System.in));

        verify(habitService, times(1)).createHabit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1);
    }

    @Test
    void handleCreateHabitNotLoggedInTest() {
        String simulatedInput = "Читать книгу\nЧитать 30 страниц ежедневно\n1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleCreateHabit(new Scanner(System.in));

        verify(habitService, never()).createHabit(anyInt(), anyString(), anyString(), anyInt());
    }

    @Test
    void handleCreateHabitInvalidFrequencyTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "Читать книгу\nЧитать 30 страниц ежедневно\ninvalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleCreateHabit(new Scanner(System.in));

        verify(habitService, never()).createHabit(anyInt(), anyString(), anyString(), anyInt());
    }

    @Test
    void handleViewHabitsTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        List<Habit> mockHabits = Arrays.asList(
                new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now()),
                new Habit(2, "Упражнения", "Утренняя зарядка", 2, 1, LocalDate.now())
        );
        when(habitService.getHabits(anyInt())).thenReturn(mockHabits);

        habitTrackerApp.handleViewHabits();

        verify(habitService, times(1)).getHabits(1);
    }

    @Test
    void handleViewHabitsNotLoggedInTest() {
        habitTrackerApp.handleViewHabits();

        verify(habitService, never()).getHabits(anyInt());
    }

    @Test
    void handleViewHabitsByDateTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = LocalDate.now().toString() + "\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        List<Habit> mockHabits = Collections.singletonList(
                new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now())
        );
        when(habitService.getHabitsByCreationDate(anyInt(), any(LocalDate.class))).thenReturn(mockHabits);

        habitTrackerApp.handleViewHabitsByDate(new Scanner(System.in));

        verify(habitService, times(1)).getHabitsByCreationDate(1, LocalDate.now());
    }

    @Test
    void handleViewHabitsByDateInvalidDateTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid-date\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleViewHabitsByDate(new Scanner(System.in));

        verify(habitService, never()).getHabitsByCreationDate(anyInt(), any(LocalDate.class));
    }

    @Test
    void handleViewHabitsByStatusTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        List<Habit> mockHabits = Collections.singletonList(
                new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now())
        );
        when(habitService.getHabitsByFrequency(anyInt(), anyInt())).thenReturn(mockHabits);

        habitTrackerApp.handleViewHabitsByStatus(new Scanner(System.in));

        verify(habitService, times(1)).getHabitsByFrequency(1, 1);
    }

    @Test
    void handleViewHabitsByStatusInvalidStatusTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleViewHabitsByStatus(new Scanner(System.in));

        verify(habitService, never()).getHabitsByFrequency(anyInt(), anyInt());
    }

    @Test
    void handleUpdateHabitTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\nНовое название\nНовое описание\n1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(habitService.updateHabit(anyInt(), anyInt(), anyString(), anyString(), anyInt())).thenReturn(true);

        habitTrackerApp.handleUpdateHabit(new Scanner(System.in));

        verify(habitService, times(1)).updateHabit(1, 1, "Новое название", "Новое описание", 1);
    }

    @Test
    void handleUpdateHabitInvalidInputTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid\nНовое название\nНовое описание\ninvalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleUpdateHabit(new Scanner(System.in));

        verify(habitService, never()).updateHabit(anyInt(), anyInt(), anyString(), anyString(), anyInt());
    }

    @Test
    void handleDeleteHabitTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(habitService.deleteHabit(anyInt(), anyInt())).thenReturn(true);

        habitTrackerApp.handleDeleteHabit(new Scanner(System.in));

        verify(habitService, times(1)).deleteHabit(1, 1);
    }

    @Test
    void handleDeleteHabitInvalidInputTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleDeleteHabit(new Scanner(System.in));

        verify(habitService, never()).deleteHabit(anyInt(), anyInt());
    }

    @Test
    void handleMarkCompleteTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        doNothing().when(habitTrackerService).markHabitCompletion(anyInt(), anyInt(), any(LocalDate.class));

        habitTrackerApp.handleMarkComplete(new Scanner(System.in));

        verify(habitTrackerService, times(1)).markHabitCompletion(1, 1, LocalDate.now());
    }

    @Test
    void handleMarkCompleteInvalidInputTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "invalid\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        habitTrackerApp.handleMarkComplete(new Scanner(System.in));

        verify(habitTrackerService, never()).markHabitCompletion(anyInt(), anyInt(), any(LocalDate.class));
    }

    @Test
    void handleViewHabitHistoryTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(habitTrackerService.getHabitHistory(anyInt(), anyInt())).thenReturn("История привычки");

        habitTrackerApp.handleViewHabitHistory(new Scanner(System.in));

        verify(habitTrackerService, times(1)).getHabitHistory(1, 1);
    }

    @Test
    void handleViewHabitStatisticsTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        String simulatedInput = "1\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        when(habitTrackerService.calculateStreak(anyInt(), anyInt())).thenReturn(5);
        when(habitTrackerService.calculateSuccessRate(anyInt(), anyInt())).thenReturn(80.0);

        habitTrackerApp.handleViewHabitStatistics(new Scanner(System.in));

        verify(habitTrackerService, times(1)).calculateStreak(1, 1);
        verify(habitTrackerService, times(1)).calculateSuccessRate(1, 1);
    }

    @Test
    void handleGenerateReportTest() {
        habitTrackerApp.setLoggedInUser(new User(1, "user@example.com", "password123", "John Doe"));
        List<Habit> mockHabits = Arrays.asList(
                new Habit(1, "Читать книгу", "Читать 30 страниц ежедневно", 1, 1, LocalDate.now()),
                new Habit(2, "Упражнения", "Утренняя зарядка", 1, 1, LocalDate.now())
        );
        when(habitService.getHabits(anyInt())).thenReturn(mockHabits);
        when(habitTrackerService.generateProgressReport(anyInt(), anyList())).thenReturn("Отчет о прогрессе");

        habitTrackerApp.handleGenerateReport();

        verify(habitService, times(1)).getHabits(1);
        verify(habitTrackerService, times(1)).generateProgressReport(1, mockHabits);
    }

    @Test
    void handleGenerateReportNotLoggedInTest() {
        habitTrackerApp.handleGenerateReport();

        verify(habitService, never()).getHabits(anyInt());
        verify(habitTrackerService, never()).generateProgressReport(anyInt(), anyList());
    }
}
