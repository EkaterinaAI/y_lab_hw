package ru.habittracker.service;

import ru.habittracker.model.Habit;
import ru.habittracker.util.IdGenerator;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HabitService {
    private final Map<Integer, List<Habit>> habits = new HashMap<>();

    public Habit createHabit(int userId, String title, String description, int frequency) {
        int habitId = IdGenerator.generateHabitId();
        Habit habit = new Habit(habitId, title, description, frequency, userId, LocalDate.now());
        habits.computeIfAbsent(userId, k -> new ArrayList<>()).add(habit);
        return habit;
    }

    public List<Habit> getHabits(int userId) {
        return new ArrayList<>(habits.getOrDefault(userId, Collections.emptyList()));
    }

    public List<Habit> getHabitsByCreationDate(int userId, LocalDate date) {
        return habits.getOrDefault(userId, Collections.emptyList())
                .stream()
                .filter(habit -> habit.getCreationDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Habit> getHabitsByFrequency(int userId, int frequency) {
        return habits.getOrDefault(userId, Collections.emptyList())
                .stream()
                .filter(habit -> habit.getFrequency() == frequency)
                .collect(Collectors.toList());
    }

    public boolean updateHabit(int userId, int habitId, String newTitle, String newDescription, int newFrequency) {
        List<Habit> userHabits = habits.get(userId);
        if (userHabits == null) return false;

        for (Habit habit : userHabits) {
            if (habit.getId() == habitId) {
                habit.setTitle(newTitle);
                habit.setDescription(newDescription);
                habit.setFrequency(newFrequency);
                return true;
            }
        }
        return false;
    }

    public boolean deleteHabit(int userId, int habitId) {
        List<Habit> userHabits = habits.get(userId);
        if (userHabits == null) return false;

        return userHabits.removeIf(habit -> habit.getId() == habitId);
    }
}
