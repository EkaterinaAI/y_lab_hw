package ru.habittracker.service;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.Habit;
import ru.habittracker.repository.HabitRepository;
import ru.habittracker.repository.interfaces.IHabitRepository;
import ru.habittracker.service.interfaces.IHabitService;

import java.time.LocalDate;
import java.util.List;

/**
 * Сервис для управления привычками пользователя.
 * <p>
 * Предоставляет методы для создания, получения, обновления и удаления привычек.
 * </p>
 *
 * author
 *     Ekaterina Ishchuk
 */
public class HabitService implements IHabitService {
    private final IHabitRepository habitRepository;

    /**
     * Конструктор сервиса привычек.
     *
     * @param dbManager менеджер подключения к базе данных
     */
    public HabitService(DatabaseConnectionManager dbManager) {
        this.habitRepository = new HabitRepository(dbManager);
    }

    /**
     * Создаёт новую привычку.
     *
     * @param userId      ID пользователя
     * @param title       название привычки
     * @param description описание привычки
     * @param frequency   частота выполнения
     * @return созданная привычка
     */
    @Override
    public Habit createHabit(int userId, String title, String description, int frequency) {
        Habit habit = new Habit(0, title, description, frequency, userId, LocalDate.now());
        return habitRepository.save(habit);
    }

    /**
     * Получает все привычки пользователя.
     *
     * @param userId ID пользователя
     * @return список привычек
     */
    @Override
    public List<Habit> getHabits(int userId) {
        return habitRepository.findByUserId(userId);
    }

    /**
     * Получает привычки пользователя по дате создания.
     *
     * @param userId ID пользователя
     * @param date   дата создания
     * @return список привычек
     */
    @Override
    public List<Habit> getHabitsByCreationDate(int userId, LocalDate date) {
        return habitRepository.findByUserIdAndCreationDate(userId, date);
    }

    /**
     * Получает привычки пользователя по частоте.
     *
     * @param userId    ID пользователя
     * @param frequency частота выполнения
     * @return список привычек
     */
    @Override
    public List<Habit> getHabitsByFrequency(int userId, int frequency) {
        return habitRepository.findByUserIdAndFrequency(userId, frequency);
    }

    /**
     * Обновляет существующую привычку.
     *
     * @param userId        ID пользователя
     * @param habitId       ID привычки
     * @param newTitle      новое название
     * @param newDescription новое описание
     * @param newFrequency  новая частота
     * @return true, если обновление прошло успешно
     */
    @Override
    public boolean updateHabit(int userId, int habitId, String newTitle, String newDescription, int newFrequency) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId);
        if (habit == null) {
            return false;
        }
        habit.setTitle(newTitle);
        habit.setDescription(newDescription);
        habit.setFrequency(newFrequency);
        return habitRepository.update(habit);
    }

    /**
     * Удаляет привычку.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @return true, если удаление прошло успешно
     */
    @Override
    public boolean deleteHabit(int userId, int habitId) {
        return habitRepository.deleteByIdAndUserId(habitId, userId);
    }
}
