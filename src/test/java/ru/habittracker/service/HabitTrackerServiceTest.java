package ru.habittracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.habittracker.model.Habit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HabitTrackerServiceTest {
    private HabitTrackerService habitTrackerService;

    @BeforeEach
    void setUp() {
        habitTrackerService = new HabitTrackerService();
    }

    @Test
    void shouldMarkHabitCompletionSuccessfully() {
        habitTrackerService.markHabitCompletion(1, 101, LocalDate.now());

        String history = habitTrackerService.getHabitHistory(1, 101);
        assertThat(history).contains(LocalDate.now().toString());
    }

    @Test
    void shouldHandleNoHistoryAvailable() {
        String history = habitTrackerService.getHabitHistory(1, 101);
        assertThat(history).isEqualTo("История отсутствует.");

    }

    @Test
    void shouldCalculateStreakSuccessfully() {
        habitTrackerService.markHabitCompletion(1, 101, LocalDate.now().minusDays(1));
        habitTrackerService.markHabitCompletion(1, 101, LocalDate.now());

        int streak = habitTrackerService.calculateStreak(1, 101);
        assertThat(streak).isEqualTo(2);
    }

    @Test
    void shouldCalculateZeroStreakWhenNoCompletion() {
        int streak = habitTrackerService.calculateStreak(1, 101);
        assertThat(streak).isEqualTo(0);
    }

    @Test
    void shouldCalculateSuccessRateSuccessfully() {
        habitTrackerService.markHabitCompletion(1, 101, LocalDate.now().minusDays(1));
        habitTrackerService.markHabitCompletion(1, 101, LocalDate.now());

        double successRate = habitTrackerService.calculateSuccessRate(1, 101);
        assertThat(successRate).isGreaterThan(0.0);
    }

    @Test
    void shouldCalculateSuccessRateWithNoCompletions() {
        double successRate = habitTrackerService.calculateSuccessRate(1, 101);
        assertThat(successRate).isEqualTo(0.0);
    }

    @Test
    void shouldGenerateProgressReportSuccessfully() {
        Habit habit = new Habit(101, "Test Habit", "Description", 1, 1, LocalDate.now());
        List<Habit> habits = new ArrayList<>();
        habits.add(habit);

        habitTrackerService.markHabitCompletion(1, 101, LocalDate.now());
        String report = habitTrackerService.generateProgressReport(1, habits);

        assertThat(report).contains("Test Habit");
        assertThat(report).contains("Текущая серия: 1 дней");
    }

    @Test
    void shouldGenerateProgressReportWithNoHabits() {
        List<Habit> habits = new ArrayList<>();
        String report = habitTrackerService.generateProgressReport(1, habits);

        assertThat(report).isEqualTo("Отчет о прогрессе:\n");
    }
}
