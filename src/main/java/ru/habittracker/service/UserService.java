package ru.habittracker.service;

import ru.habittracker.model.User;
import ru.habittracker.util.IdGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserService {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<String, Integer> emailToUserId = new HashMap<>(); // Для отслеживания уникальности email

    // Регистрация пользователя с проверкой уникальности email
    public Optional<User> registerUser(String email, String password, String name) {
        if (emailToUserId.containsKey(email)) {
            System.out.println("Пользователь с таким email уже существует.");
            return Optional.empty();
        }

        int id = IdGenerator.generateUserId();
        User user = new User(id, email, password, name);

        users.put(id, user);
        emailToUserId.put(email, id);

        return Optional.of(user);
    }

    // Аутентификация пользователя по email и паролю
    public Optional<User> loginUser(String email, String password) {
        Integer userId = emailToUserId.get(email);
        if (userId == null) {
            return Optional.empty();
        }
        User user = users.get(userId);
        return (user != null && user.getPassword().equals(password)) ? Optional.of(user) : Optional.empty();
    }

    // Обновление профиля пользователя
    public boolean updateUser(int userId, String newEmail, String newPassword, String newName) {
        User user = users.get(userId);
        if (user == null) return false;

        if (!user.getEmail().equals(newEmail) && emailToUserId.containsKey(newEmail)) {
            System.out.println("Email уже используется другим пользователем.");
            return false;
        }

        emailToUserId.remove(user.getEmail());
        user.setEmail(newEmail);
        user.setPassword(newPassword);
        user.setName(newName);
        emailToUserId.put(newEmail, userId);

        return true;
    }

    // Удаление пользователя
    public boolean deleteUser(int userId) {
        User user = users.remove(userId);
        if (user != null) {
            emailToUserId.remove(user.getEmail());
            return true;
        }
        return false;
    }
}
