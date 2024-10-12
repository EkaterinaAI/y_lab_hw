package ru.habittracker.service;

import ru.habittracker.model.Habit;
import ru.habittracker.model.HabitRecord;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HabitTrackerService {
    private final Map<Integer, List<HabitRecord>> habitRecords = new HashMap<>();

    // Метод для отметки выполнения привычки
    public void markHabitCompletion(int userId, int habitId, LocalDate date) {
        List<HabitRecord> userRecords = habitRecords.computeIfAbsent(userId, k -> new ArrayList<>());

        // Проверяем, существует ли уже запись для данной привычки и даты
        Optional<HabitRecord> existingRecord = userRecords.stream()
                .filter(record -> record.getHabitId() == habitId && record.getDate().equals(date))
                .findFirst();

        if (existingRecord.isPresent()) {
            existingRecord.get().setCompleted(true);
        } else {
            userRecords.add(new HabitRecord(habitId, date, true));
        }

        System.out.println("Привычка отмечена как выполненная за " + date + ".");
    }

    // Метод для вывода истории выполнения привычки
    public String getHabitHistory(int userId, int habitId) {
        List<HabitRecord> records = habitRecords.getOrDefault(userId, Collections.emptyList());

        String history = records.stream()
                .filter(record -> record.getHabitId() == habitId)
                .sorted(Comparator.comparing(HabitRecord::getDate))
                .map(record -> record.toString())
                .collect(Collectors.joining("\n"));

        return history.isEmpty() ? "История отсутствует." : history;
    }

    // Метод для подсчета текущей серии выполнения привычки
    public int calculateStreak(int userId, int habitId) {
        List<HabitRecord> records = habitRecords.getOrDefault(userId, Collections.emptyList())
                .stream()
                .filter(record -> record.getHabitId() == habitId && record.isCompleted())
                .sorted(Comparator.comparing(HabitRecord::getDate).reversed())
                .collect(Collectors.toList());

        int streak = 0;
        LocalDate currentDate = LocalDate.now();

        for (HabitRecord record : records) {
            if (record.getDate().equals(currentDate) || record.getDate().equals(currentDate.minusDays(1))) {
                streak++;
                currentDate = record.getDate();
            } else {
                break;
            }
        }
        return streak;
    }

    // Метод для подсчета процента успешного выполнения привычки за последний месяц
    public double calculateSuccessRate(int userId, int habitId) {
        List<HabitRecord> records = habitRecords.getOrDefault(userId, Collections.emptyList());
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        long totalDays = 30;
        long completedDays = records.stream()
                .filter(record -> record.getHabitId() == habitId && record.isCompleted() && !record.getDate().isBefore(thirtyDaysAgo))
                .count();

        return (double) completedDays / totalDays * 100;
    }

    // Метод для генерации отчета по прогрессу выполнения всех привычек пользователя
    public String generateProgressReport(int userId, List<Habit> habits) {
        StringBuilder report = new StringBuilder("Отчет о прогрессе:\n");

        for (Habit habit : habits) {
            int streak = calculateStreak(userId, habit.getId());
            double successRate = calculateSuccessRate(userId, habit.getId());

            report.append("Привычка: ").append(habit.getTitle()).append("\n")
                    .append("Частота: ").append(habit.getFrequency() == 1 ? "Ежедневная" : "Недельная").append("\n")
                    .append("Текущая серия: ").append(streak).append(" дней\n")
                    .append("Процент успеха: ").append(String.format("%.2f", successRate)).append("% за последний месяц\n")
                    .append("----------\n");
        }

        return report.toString();
    }
}
