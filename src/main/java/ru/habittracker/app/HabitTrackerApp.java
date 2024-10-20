package ru.habittracker.app;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.controller.HabitTrackerController;
import ru.habittracker.service.HabitService;
import ru.habittracker.service.HabitTrackerService;
import ru.habittracker.service.UserService;
import ru.habittracker.service.interfaces.IHabitService;
import ru.habittracker.service.interfaces.IHabitTrackerService;
import ru.habittracker.service.interfaces.IUserService;

/**
 * Главный класс приложения "Трекер Привычек".
 * <p>
 * Запускает приложение и инициализирует необходимые сервисы и контроллер.
 * </p>
 *
 * @author
 *      Ekaterina Ishchuk
 */
public class HabitTrackerApp {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        DatabaseConnectionManager dbManager = new DatabaseConnectionManager();
        IUserService userService = new UserService(dbManager);
        IHabitService habitService = new HabitService(dbManager);
        IHabitTrackerService habitTrackerService = new HabitTrackerService(dbManager);

        HabitTrackerController controller = new HabitTrackerController(userService, habitService, habitTrackerService);
        controller.run();
    }
}
