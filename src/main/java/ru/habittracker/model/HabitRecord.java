package ru.habittracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class HabitRecord {
    private int habitId;
    private LocalDate date;
    private boolean completed;

    @Override
    public String toString() {
        return String.format("Дата: %s, Выполнено: %s", date, completed ? "Да" : "Нет");
    }
}
