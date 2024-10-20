package ru.habittracker.service;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.Habit;
import ru.habittracker.model.HabitRecord;
import ru.habittracker.repository.HabitRecordRepository;
import ru.habittracker.repository.interfaces.IHabitRecordRepository;
import ru.habittracker.service.interfaces.IHabitTrackerService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для отслеживания выполнения привычек.
 * <p>
 * Предоставляет методы для отметки выполнения привычек, получения истории, расчёта статистики и генерации отчётов.
 * </p>
 * <p>
 * Связанные классы:
 * <ul>
 *     <li>{@link IHabitTrackerService}</li>
 *     <li>{@link HabitRecordRepository}</li>
 *     <li>{@link HabitRecord}</li>
 *     <li>{@link Habit}</li>
 * </ul>
 * </p>
 *
 * @author
 *     Ekaterina Ishchuk
 */
public class HabitTrackerService implements IHabitTrackerService {
    private final IHabitRecordRepository habitRecordRepository;

    /**
     * Конструктор сервиса отслеживания привычек.
     *
     * @param dbManager менеджер подключения к базе данных
     */
    public HabitTrackerService(DatabaseConnectionManager dbManager) {
        this.habitRecordRepository = new HabitRecordRepository(dbManager);
    }

    /**
     * Отмечает выполнение привычки в указанную дату.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @param date    дата выполнения
     */
    @Override
    public void markHabitCompletion(int userId, int habitId, LocalDate date) {
        HabitRecord record = new HabitRecord(habitId, date, true);
        habitRecordRepository.save(record);

        System.out.println("Привычка отмечена как выполненная за " + date + ".");
    }

    /**
     * Получает историю выполнения привычки.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @return строка с историей выполнения
     */
    @Override
    public String getHabitHistory(int userId, int habitId) {
        List<HabitRecord> records = habitRecordRepository.findByHabitId(habitId);

        String history = records.stream()
                .sorted(Comparator.comparing(HabitRecord::getDate))
                .map(HabitRecord::toString)
                .collect(Collectors.joining("\n"));

        return history.isEmpty() ? "История отсутствует." : history;
    }

    /**
     * Вычисляет текущую серию выполнения привычки.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @return количество дней текущей серии
     */
    @Override
    public int calculateStreak(int userId, int habitId) {
        List<HabitRecord> records = habitRecordRepository.findByHabitId(habitId)
                .stream()
                .filter(HabitRecord::isCompleted)
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

    /**
     * Вычисляет процент успешного выполнения привычки за последний месяц.
     *
     * @param userId  ID пользователя
     * @param habitId ID привычки
     * @return процент успешного выполнения
     */
    @Override
    public double calculateSuccessRate(int userId, int habitId) {
        List<HabitRecord> records = habitRecordRepository.findByHabitId(habitId);
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        long totalDays = 30;
        long completedDays = records.stream()
                .filter(record -> record.isCompleted() && !record.getDate().isBefore(thirtyDaysAgo))
                .count();

        return (double) completedDays / totalDays * 100;
    }

    /**
     * Генерирует отчёт о прогрессе по всем привычкам пользователя.
     *
     * @param userId ID пользователя
     * @param habits список привычек
     * @return строка с отчётом о прогрессе
     */
    @Override
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
