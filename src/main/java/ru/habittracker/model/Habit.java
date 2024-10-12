package ru.habittracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class Habit {
    private int id;
    private String title;
    private String description;
    private int frequency; // 1 - daily, 2 - weekly
    private int userId;
    private LocalDate creationDate;

    @Override
    public String toString() {
        String freq = frequency == 1 ? "Ежедневная" : "Недельная";
        return String.format("ID: %d, Название: %s, Описание: %s, Частота: %s, Дата создания: %s",
                id, title, description, freq, creationDate);
    }
}
