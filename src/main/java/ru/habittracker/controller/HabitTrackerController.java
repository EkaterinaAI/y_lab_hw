package ru.habittracker.controller;

import ru.habittracker.model.Habit;
import ru.habittracker.model.User;
import ru.habittracker.service.IHabitService;
import ru.habittracker.service.IHabitTrackerService;
import ru.habittracker.service.IUserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Контроллер для взаимодействия с пользователем и обработки команд.
 * <p>
 * Обрабатывает пользовательский ввод и взаимодействует с соответствующими сервисами.
 * </p>
 *
 * author
 *      Ekaterina Ishchuk
 */
public class HabitTrackerController {

    private final IUserService userService;
    private final IHabitService habitService;
    private final IHabitTrackerService habitTrackerService;
    private User loggedInUser = null;

    /**
     * Конструктор контроллера.
     *
     * @param userService         сервис для работы с пользователями
     * @param habitService        сервис для работы с привычками
     * @param habitTrackerService сервис для отслеживания выполнения привычек
     */
    public HabitTrackerController(IUserService userService, IHabitService habitService, IHabitTrackerService habitTrackerService) {
        this.userService = userService;
        this.habitService = habitService;
        this.habitTrackerService = habitTrackerService;
    }

    /**
     * Запускает основной цикл приложения для обработки пользовательских команд.
     */
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Добро пожаловать в Трекер Привычек!");

            while (true) {
                displayMenu();
                System.out.print("Ваш выбор: ");

                int command;
                try {
                    command = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Неверный ввод команды. Пожалуйста, введите число от 1 до 16.");
                    continue;
                }

                switch (command) {
                    case 1 -> handleRegister(scanner);
                    case 2 -> handleLogin(scanner);
                    case 3 -> handleUpdateUser(scanner);
                    case 4 -> handleDeleteUser(scanner);
                    case 5 -> handleCreateHabit(scanner);
                    case 6 -> handleViewHabits();
                    case 7 -> handleViewHabitsByDate(scanner);
                    case 8 -> handleViewHabitsByStatus(scanner);
                    case 9 -> handleUpdateHabit(scanner);
                    case 10 -> handleDeleteHabit(scanner);
                    case 11 -> handleMarkComplete(scanner);
                    case 12 -> handleViewHabitHistory(scanner);
                    case 13 -> handleViewHabitStatistics(scanner);
                    case 14 -> handleGenerateReport();
                    case 15 -> {
                        loggedInUser = null;
                        System.out.println("Вы вышли из системы.");
                    }
                    case 16 -> {
                        System.out.println("До свидания!");
                        return;  // Завершение метода, если это необходимо
                    }
                    default -> System.out.println("Неверная команда.");
                }

            }
        }
    }

    /**
     * Выводит меню команд.
     */
    private void displayMenu() {
        System.out.println("Введите номер команды: ");
        System.out.println("1 - Регистрация пользователя");
        System.out.println("2 - Войти в систему");
        System.out.println("3 - Обновить данные пользователя");
        System.out.println("4 - Удалить пользователя");
        System.out.println("5 - Создать привычку");
        System.out.println("6 - Посмотреть все привычки");
        System.out.println("7 - Выбрать привычки по дате");
        System.out.println("8 - Выбрать привычки по частоте (1 - ежедневная, 2 - недельная)");
        System.out.println("9 - Обновить существующую привычку");
        System.out.println("10 - Удалить привычку");
        System.out.println("11 - Отметить выполнение привычки");
        System.out.println("12 - Посмотреть историю выполнения");
        System.out.println("13 - Посмотреть статистику выполнения");
        System.out.println("14 - Сформировать отчет по прогрессу");
        System.out.println("15 - Выйти из системы");
        System.out.println("16 - Завершить работу программы");
    }

    /**
     * Обрабатывает регистрацию нового пользователя.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleRegister(Scanner scanner) {
        System.out.print("Введите email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();
        System.out.print("Введите имя: ");
        String name = scanner.nextLine().trim();

        Optional<User> user = userService.registerUser(email, password, name);
        user.ifPresentOrElse(
                u -> System.out.println("Пользователь успешно зарегистрирован."),
                () -> System.out.println("Регистрация не удалась. Возможно, email уже используется.")
        );
    }

    /**
     * Обрабатывает вход пользователя в систему.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleLogin(Scanner scanner) {
        System.out.print("Введите email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        Optional<User> user = userService.loginUser(email, password);
        if (user.isPresent()) {
            loggedInUser = user.get();
            System.out.println("Успешный вход в систему.");
        } else {
            System.out.println("Неверный email или пароль.");
        }
    }

    /**
     * Обрабатывает обновление данных пользователя.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleUpdateUser(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для обновления профиля.");
            return;
        }

        System.out.print("Введите новый email: ");
        String newEmail = scanner.nextLine().trim();
        System.out.print("Введите новый пароль: ");
        String newPassword = scanner.nextLine().trim();
        System.out.print("Введите новое имя: ");
        String newName = scanner.nextLine().trim();

        boolean updated = userService.updateUser(loggedInUser.getId(), newEmail, newPassword, newName);
        if (updated) {
            System.out.println("Профиль успешно обновлен.");
        } else {
            System.out.println("Не удалось обновить профиль. Возможно, email уже используется.");
        }
    }

    /**
     * Обрабатывает удаление аккаунта пользователя.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleDeleteUser(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для удаления аккаунта.");
            return;
        }

        System.out.print("Вы уверены, что хотите удалить аккаунт? Введите 'yes' для подтверждения: ");
        String confirmation = scanner.nextLine().trim();

        if ("yes".equalsIgnoreCase(confirmation)) {
            boolean deleted = userService.deleteUser(loggedInUser.getId());
            if (deleted) {
                System.out.println("Аккаунт успешно удален.");
                loggedInUser = null;
            } else {
                System.out.println("Не удалось удалить аккаунт.");
            }
        } else {
            System.out.println("Удаление аккаунта отменено.");
        }
    }

    /**
     * Обрабатывает создание новой привычки.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleCreateHabit(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для создания привычки.");
            return;
        }
        System.out.print("Введите название привычки: ");
        String title = scanner.nextLine().trim();
        System.out.print("Введите описание привычки: ");
        String description = scanner.nextLine().trim();
        System.out.print("Введите частоту (1 - ежедневная, 2 - недельная): ");
        int frequency;
        try {
            frequency = Integer.parseInt(scanner.nextLine().trim());
            if (frequency != 1 && frequency != 2) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            System.out.println("Неверный ввод. Пожалуйста, введите число 1 или 2.");
            return;
        }

        habitService.createHabit(loggedInUser.getId(), title, description, frequency);
        System.out.println("Привычка успешно создана.");
    }

    /**
     * Обрабатывает просмотр всех привычек пользователя.
     */
    public void handleViewHabits() {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для просмотра привычек.");
            return;
        }
        List<Habit> habits = habitService.getHabits(loggedInUser.getId());
        if (habits.isEmpty()) {
            System.out.println("У вас нет созданных привычек.");
        } else {
            habits.forEach(habit -> System.out.println(habit));
        }
    }

    /**
     * Обрабатывает просмотр привычек по дате создания.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleViewHabitsByDate(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для просмотра привычек по дате.");
            return;
        }

        System.out.print("Введите дату создания (yyyy-mm-dd): ");
        String dateInput = scanner.nextLine().trim();
        LocalDate date;

        try {
            date = LocalDate.parse(dateInput);
        } catch (Exception e) {
            System.out.println("Неверный формат даты. Используйте yyyy-mm-dd.");
            return;
        }

        List<Habit> habitsByDate = habitService.getHabitsByCreationDate(loggedInUser.getId(), date);
        if (habitsByDate.isEmpty()) {
            System.out.println("Привычки не найдены на указанную дату.");
        } else {
            habitsByDate.forEach(habit -> System.out.println(habit));
        }
    }

    /**
     * Обрабатывает просмотр привычек по частоте.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleViewHabitsByStatus(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для просмотра привычек по частоте.");
            return;
        }

        System.out.print("Введите частоту для фильтрации (1 - ежедневная, 2 - недельная): ");
        int frequency;
        try {
            frequency = Integer.parseInt(scanner.nextLine().trim());
            if (frequency != 1 && frequency != 2) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            System.out.println("Неверный ввод. Пожалуйста, введите 1 для ежедневной или 2 для недельной.");
            return;
        }

        List<Habit> filteredHabits = habitService.getHabitsByFrequency(loggedInUser.getId(), frequency);
        if (filteredHabits.isEmpty()) {
            System.out.println("Привычки не найдены для указанной частоты.");
        } else {
            filteredHabits.forEach(habit -> System.out.println(habit));
        }
    }

    /**
     * Обрабатывает обновление существующей привычки.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleUpdateHabit(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для обновления привычек.");
            return;
        }
        System.out.print("Введите ID привычки: ");
        int habitId;
        try {
            habitId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат ID.");
            return;
        }
        System.out.print("Введите новое название: ");
        String newTitle = scanner.nextLine().trim();
        System.out.print("Введите новое описание: ");
        String newDescription = scanner.nextLine().trim();
        System.out.print("Введите новую частоту (1 - ежедневная, 2 - недельная): ");
        int newFrequency;
        try {
            newFrequency = Integer.parseInt(scanner.nextLine().trim());
            if (newFrequency != 1 && newFrequency != 2) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            System.out.println("Неверный ввод. Пожалуйста, введите 1 или 2.");
            return;
        }

        boolean updated = habitService.updateHabit(loggedInUser.getId(), habitId, newTitle, newDescription, newFrequency);
        if (updated) {
            System.out.println("Привычка успешно обновлена.");
        } else {
            System.out.println("Не удалось обновить привычку. Проверьте ID и попробуйте снова.");
        }
    }

    /**
     * Обрабатывает удаление привычки.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleDeleteHabit(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для удаления привычек.");
            return;
        }
        System.out.print("Введите ID привычки для удаления: ");
        int habitId;
        try {
            habitId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат ID.");
            return;
        }

        boolean deleted = habitService.deleteHabit(loggedInUser.getId(), habitId);
        if (deleted) {
            System.out.println("Привычка успешно удалена.");
        } else {
            System.out.println("Не удалось удалить привычку. Проверьте ID и попробуйте снова.");
        }
    }

    /**
     * Обрабатывает отметку выполнения привычки.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleMarkComplete(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для отметки привычки как выполненной.");
            return;
        }
        System.out.print("Введите ID привычки для отметки выполнения: ");
        int habitId;
        try {
            habitId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат ID.");
            return;
        }

        habitTrackerService.markHabitCompletion(loggedInUser.getId(), habitId, LocalDate.now());
    }

    /**
     * Обрабатывает просмотр истории выполнения привычки.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleViewHabitHistory(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для просмотра истории привычки.");
            return;
        }
        System.out.print("Введите ID привычки для просмотра истории: ");
        int habitId;
        try {
            habitId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат ID.");
            return;
        }

        String history = habitTrackerService.getHabitHistory(loggedInUser.getId(), habitId);
        System.out.println("История привычки:\n" + history);
    }

    /**
     * Обрабатывает просмотр статистики выполнения привычки.
     *
     * @param scanner объект {@link Scanner} для чтения пользовательского ввода
     */
    public void handleViewHabitStatistics(Scanner scanner) {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для просмотра статистики привычки.");
            return;
        }

        System.out.print("Введите ID привычки для просмотра статистики: ");
        int habitId;
        try {
            habitId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат ID.");
            return;
        }

        int streak = habitTrackerService.calculateStreak(loggedInUser.getId(), habitId);
        double successRate = habitTrackerService.calculateSuccessRate(loggedInUser.getId(), habitId);

        System.out.println("ID Привычки: " + habitId);
        System.out.println("Текущая серия: " + streak + " дней");
        System.out.println("Процент успеха: " + String.format("%.2f", successRate) + "% за последний месяц");
    }

    /**
     * Обрабатывает генерацию отчета по прогрессу пользователя.
     */
    public void handleGenerateReport() {
        if (loggedInUser == null) {
            System.out.println("Вы должны войти в систему для генерации отчета.");
            return;
        }

        List<Habit> habits = habitService.getHabits(loggedInUser.getId());
        String report = habitTrackerService.generateProgressReport(loggedInUser.getId(), habits);
        System.out.println(report);
    }

    /**
     * Получает текущего авторизованного пользователя.
     *
     * @return объект {@link User}
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Устанавливает текущего авторизованного пользователя.
     *
     * @param user объект {@link User}
     */
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }
}
