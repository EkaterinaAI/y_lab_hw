package ru.habittracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private int id;
    private String email;
    private String password;
    private String name;

    @Override
    public String toString() {
        return String.format("ID: %d, Имя: %s, Email: %s", id, name, email);
    }
}
