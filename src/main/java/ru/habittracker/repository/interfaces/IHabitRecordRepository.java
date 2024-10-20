package ru.habittracker.repository.interfaces;

import ru.habittracker.model.HabitRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для репозитория записей привычек.
 * <p>
 * Определяет методы для сохранения, удаления и поиска записей о выполнении привычек.
 * </p>
 * <p>
 * Связанные классы:
 * <ul>
 *     <li>{@link HabitRecord}</li>
 * </ul>
 * </p>
 *
 * author
 *     Ekaterina Ishchuk
 */
public interface IHabitRecordRepository {
    /**
     * Сохраняет новую запись о выполнении привычки.
     *
     * @param record объект записи для сохранения
     * @return сохранённый объект записи с установленным ID
     */
    Optional<HabitRecord> save(HabitRecord record);

    /**
     * Находит запись о выполнении привычки по ID.
     *
     * @param id ID записи
     * @return объект записи или пустой Optional, если не найдено
     */
    Optional<HabitRecord> findById(int id);

    /**
     * Находит все записи о выполнении определённой привычки.
     *
     * @param habitId ID привычки
     * @return список записей
     */
    List<HabitRecord> findByHabitId(int habitId);

    /**
     * Находит записи о выполнении привычек пользователя за определённую дату.
     *
     * @param userId ID пользователя
     * @param date   дата
     * @return список записей
     */
    List<HabitRecord> findByUserIdAndDate(int userId, LocalDate date);

    /**
     * Удаляет запись о выполнении привычки по ID.
     *
     * @param id ID записи
     * @return true, если удаление прошло успешно
     */
    boolean delete(int id);
}
