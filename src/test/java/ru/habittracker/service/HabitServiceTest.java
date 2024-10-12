package ru.habittracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.habittracker.model.Habit;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HabitServiceTest {
    private HabitService habitService;

    @BeforeEach
    void setUp() {
        habitService = new HabitService();
    }

    @Test
    void shouldCreateHabitSuccessfully() {
        Habit habit = habitService.createHabit(1, "Test Habit", "Description", 1);
        assertThat(habit.getTitle()).isEqualTo("Test Habit");
        assertThat(habit.getFrequency()).isEqualTo(1);
    }

    @Test
    void shouldUpdateHabitSuccessfully() {
        Habit habit = habitService.createHabit(1, "Test Habit", "Description", 1);
        boolean isUpdated = habitService.updateHabit(1, habit.getId(), "New Title", "New Description", 2);

        assertTrue(isUpdated);
        List<Habit> habits = habitService.getHabits(1);
        assertThat(habits).hasSize(1);
        assertThat(habits.get(0).getTitle()).isEqualTo("New Title");
        assertThat(habits.get(0).getFrequency()).isEqualTo(2);
    }

    @Test
    void shouldNotUpdateNonexistentHabit() {
        boolean isUpdated = habitService.updateHabit(1, 999, "New Title", "New Description", 2);
        assertFalse(isUpdated);
    }

    @Test
    void shouldDeleteHabitSuccessfully() {
        Habit habit = habitService.createHabit(1, "Test Habit", "Description", 1);
        boolean isDeleted = habitService.deleteHabit(1, habit.getId());

        assertTrue(isDeleted);
        List<Habit> habits = habitService.getHabits(1);
        assertThat(habits).isEmpty();
    }

    @Test
    void shouldNotDeleteNonexistentHabit() {
        boolean isDeleted = habitService.deleteHabit(1, 999);
        assertFalse(isDeleted);
    }

    @Test
    void shouldReturnEmptyListForNonexistentUser() {
        List<Habit> habits = habitService.getHabits(999);
        assertThat(habits).isEmpty();
    }

    @Test
    void shouldReturnEmptyListForInvalidFrequency() {
        habitService.createHabit(1, "Test Habit", "Description", 1);
        List<Habit> habits = habitService.getHabitsByFrequency(1, 3); // Некорректная частота

        assertThat(habits).isEmpty();
    }

    @Test
    void shouldFilterHabitsByCreationDate() {
        Habit habit1 = habitService.createHabit(1, "Habit1", "Desc1", 1);
        habit1.setCreationDate(LocalDate.now().minusDays(1));
        Habit habit2 = habitService.createHabit(1, "Habit2", "Desc2", 1);

        List<Habit> habits = habitService.getHabitsByCreationDate(1, LocalDate.now());

        assertThat(habits).hasSize(1);
        assertThat(habits.get(0).getTitle()).isEqualTo("Habit2");
    }
}
