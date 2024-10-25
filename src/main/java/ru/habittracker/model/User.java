package ru.habittracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Класс, представляющий пользователя приложения.
 *
 * author
 *      Ekaterina Ishchuk
 */
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

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}
