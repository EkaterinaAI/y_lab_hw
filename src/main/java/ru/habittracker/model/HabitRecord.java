package ru.habittracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Класс, представляющий запись выполнения привычки.
 *
 * author
 *      Ekaterina Ishchuk
 */
@Getter
@Setter
@AllArgsConstructor
public class HabitRecord {
    private int id;
    private int habitId;
    private LocalDate date;
    private boolean completed;

    /**
     * Конструктор без ID для создания новых записей.
     *
     * @param habitId   ID привычки
     * @param date      дата выполнения
     * @param completed статус выполнения
     */
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
