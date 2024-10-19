package ru.habittracker.service;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.Habit;
import ru.habittracker.repository.HabitRepository;

import java.time.LocalDate;
import java.util.List;

public class HabitService {
    private final HabitRepository habitRepository;

    public HabitService(DatabaseConnectionManager dbManager) {
        this.habitRepository = new HabitRepository(dbManager);
    }

    public Habit createHabit(int userId, String title, String description, int frequency) {
        Habit habit = new Habit(0, title, description, frequency, userId, LocalDate.now());
        return habitRepository.save(habit);
    }

    public List<Habit> getHabits(int userId) {
        return habitRepository.findByUserId(userId);
    }

    public List<Habit> getHabitsByCreationDate(int userId, LocalDate date) {
        return habitRepository.findByUserIdAndCreationDate(userId, date);
    }

    public List<Habit> getHabitsByFrequency(int userId, int frequency) {
        return habitRepository.findByUserIdAndFrequency(userId, frequency);
    }

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

    public boolean deleteHabit(int userId, int habitId) {
        return habitRepository.deleteByIdAndUserId(habitId, userId);
    }
}
