package ru.habittracker.repository;

import ru.habittracker.model.Habit;

import java.time.LocalDate;
import java.util.List;

/**
 * Интерфейс для репозитория привычек.
 * <p>
 * Определяет методы для сохранения, обновления, удаления и поиска привычек.
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
public interface IHabitRepository {
    /**
     * Сохраняет новую привычку.
     *
     * @param habit объект привычки для сохранения
     * @return сохранённый объект привычки с установленным ID
     */
    Habit save(Habit habit);

    /**
     * Находит привычку по ID и ID пользователя.
     *
     * @param id     ID привычки
     * @param userId ID пользователя
     * @return объект привычки или null, если не найдено
     */
    Habit findByIdAndUserId(int id, int userId);

    /**
     * Находит все привычки пользователя.
     *
     * @param userId ID пользователя
     * @return список привычек
     */
    List<Habit> findByUserId(int userId);

    /**
     * Находит привычки пользователя по дате создания.
     *
     * @param userId ID пользователя
     * @param date   дата создания
     * @return список привычек
     */
    List<Habit> findByUserIdAndCreationDate(int userId, LocalDate date);

    /**
     * Находит привычки пользователя по частоте.
     *
     * @param userId    ID пользователя
     * @param frequency частота выполнения
     * @return список привычек
     */
    List<Habit> findByUserIdAndFrequency(int userId, int frequency);

    /**
     * Обновляет информацию о привычке.
     *
     * @param habit объект привычки с обновлёнными данными
     * @return true, если обновление прошло успешно
     */
    boolean update(Habit habit);

    /**
     * Удаляет привычку по ID и ID пользователя.
     *
     * @param id     ID привычки
     * @param userId ID пользователя
     * @return true, если удаление прошло успешно
     */
    boolean deleteByIdAndUserId(int id, int userId);
}
