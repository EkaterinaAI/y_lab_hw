package ru.habittracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class HabitRecord {
    private int id;
    private int habitId;
    private LocalDate date;
    private boolean completed;

    public HabitRecord(int habitId, LocalDate date, boolean completed) {
        this.habitId = habitId;
        this.date = date;
        this.completed = completed;
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Дата: %s, Выполнено: %s", id, date, completed ? "Да" : "Нет");
    }
}
