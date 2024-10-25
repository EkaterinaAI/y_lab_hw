package ru.habittracker.service.impl;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.Habit;
import ru.habittracker.repository.impl.HabitRepository;
import ru.habittracker.repository.IHabitRepository;
import ru.habittracker.service.IHabitService;

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

    @Override
    public Habit createHabit(int userId, String title, String description, int frequency) {
        Habit habit = new Habit(0, title, description, frequency, userId, LocalDate.now());
        return habitRepository.save(habit);
    }

    @Override
    public List<Habit> getHabits(int userId) {
        return habitRepository.findByUserId(userId);
    }

    @Override
    public List<Habit> getHabitsByCreationDate(int userId, LocalDate date) {
        return habitRepository.findByUserIdAndCreationDate(userId, date);
    }

    @Override
    public List<Habit> getHabitsByFrequency(int userId, int frequency) {
        return habitRepository.findByUserIdAndFrequency(userId, frequency);
    }

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

    @Override
    public boolean deleteHabit(int userId, int habitId) {
        return habitRepository.deleteByIdAndUserId(habitId, userId);
    }
}
