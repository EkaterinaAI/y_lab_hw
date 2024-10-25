package ru.habittracker.service;

import ru.habittracker.model.Habit;

import java.time.LocalDate;
import java.util.List;

/**
 * Интерфейс для сервиса управления привычками.
 * <p>
 * Определяет методы для создания, получения, обновления и удаления привычек.
 * </p>
 *
 * author
 *     Ekaterina Ishchuk
 */
public interface IHabitService {
    /**
     * Создаёт новую привычку.
     *
     * @param userId      ID пользователя
     * @param title       название привычки
     * @param description описание привычки
     * @param frequency   частота выполнения
     * @return созданная привычка
     */
    Habit createHabit(int userId, String title, String description, int frequency);

    /**
     * Получает все привычки пользователя.
     *
     * @param userId ID пользователя
     * @return список привычек
     */
    List<Habit> getHabits(int userId);

    /**
     * Получает привычки пользователя по дате создания.
     *
     * @param userId ID пользователя
     * @param date   дата создания
     * @return список привычек
     */
    List<Habit> getHabitsByCreationDate(int userId, LocalDate date);

    /**
     * Получает привычки пользователя по частоте.
     *
     * @param userId    ID пользователя
     * @param frequency частота выполнения
     * @return список привычек
     */
    List<Habit> getHabitsByFrequency(int userId, int frequency);

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
    boolean updateHabit(int userId, int habitId, String newTitle, String newDescription, int newFrequency);

    /**
     * Удаляет привычку.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @return true, если удаление прошло успешно
     */
    boolean deleteHabit(int userId, int habitId);
}
