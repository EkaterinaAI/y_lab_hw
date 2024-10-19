package ru.habittracker.service;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.User;
import ru.habittracker.repository.UserRepository;

import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    // Конструктор с внедрением зависимости
    public UserService(DatabaseConnectionManager dbManager) {
        this.userRepository = new UserRepository(dbManager);
    }

    // Регистрация пользователя с проверкой уникальности email
    public Optional<User> registerUser(String email, String password, String name) {
        // Проверка на уникальность email
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            System.out.println("Пользователь с таким email уже существует.");
            return Optional.empty();
        }

        User user = new User(0, email, password, name); // ID будет установлен репозиторием
        return userRepository.save(user);
    }

    // Аутентификация пользователя по email и паролю
    public Optional<User> loginUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        return Optional.empty();
    }

    // Обновление профиля пользователя
    public boolean updateUser(int userId, String newEmail, String newPassword, String newName) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) return false;

        // Проверка на уникальность нового email
        Optional<User> emailUser = userRepository.findByEmail(newEmail);
        if (emailUser.isPresent() && emailUser.get().getId() != userId) {
            System.out.println("Email уже используется другим пользователем.");
            return false;
        }

        User user = userOptional.get();
        user.setEmail(newEmail);
        user.setPassword(newPassword);
        user.setName(newName);

        return userRepository.update(user);
    }

    // Удаление пользователя
    public boolean deleteUser(int userId) {
        return userRepository.delete(userId);
    }
}
