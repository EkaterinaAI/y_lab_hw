package ru.habittracker.service.interfaces;

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
    Habit createHabit(int userId, String title, String description, int frequency);
    List<Habit> getHabits(int userId);
    List<Habit> getHabitsByCreationDate(int userId, LocalDate date);
    List<Habit> getHabitsByFrequency(int userId, int frequency);
    boolean updateHabit(int userId, int habitId, String newTitle, String newDescription, int newFrequency);
    boolean deleteHabit(int userId, int habitId);
}
