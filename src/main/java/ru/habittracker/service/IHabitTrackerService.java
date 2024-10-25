package ru.habittracker.service;

import ru.habittracker.model.Habit;

import java.time.LocalDate;
import java.util.List;

/**
 * Интерфейс для сервиса отслеживания выполнения привычек.
 * <p>
 * Определяет методы для отметки выполнения, получения истории, расчёта статистики и генерации отчётов.
 * </p>
 * <p>
 * Связанные классы:
 * <ul>
 *     <li>{@link Habit}</li>
 * </ul>
 * </p>
 *
 * author
 *     Ekaterina Ishchuk
 */
public interface IHabitTrackerService {
    /**
     * Отмечает выполнение привычки в указанную дату.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @param date    дата выполнения
     */
    void markHabitCompletion(int userId, int habitId, LocalDate date);

    /**
     * Получает историю выполнения привычки.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @return строка с историей выполнения
     */
    String getHabitHistory(int userId, int habitId);

    /**
     * Вычисляет текущую серию выполнения привычки.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @return количество дней текущей серии
     */
    int calculateStreak(int userId, int habitId);

    /**
     * Вычисляет процент успешного выполнения привычки за последний месяц.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @return процент успешного выполнения
     */
    double calculateSuccessRate(int userId, int habitId);

    /**
     * Генерирует отчёт о прогрессе по всем привычкам пользователя.
     *
     * @param userId ID пользователя
     * @param habits список привычек
     * @return строка с отчётом о прогрессе
     */
    String generateProgressReport(int userId, List<Habit> habits);
}
