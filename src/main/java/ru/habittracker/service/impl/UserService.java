package ru.habittracker.service.impl;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.User;
import ru.habittracker.repository.impl.UserRepository;
import ru.habittracker.repository.IUserRepository;
import ru.habittracker.service.IUserService;

import java.util.Optional;

/**
 * Сервис для управления пользователями.
 * <p>
 * Предоставляет методы для регистрации, входа, обновления и удаления пользователей.
 * </p>
 *
 * author
 *     Ekaterina Ishchuk
 */
public class UserService implements IUserService {
    private final IUserRepository userRepository;

    /**
     * Конструктор сервиса пользователей.
     *
     * @param dbManager менеджер подключения к базе данных
     */
    public UserService(DatabaseConnectionManager dbManager) {
        this.userRepository = new UserRepository(dbManager);
    }

    @Override
    public Optional<User> registerUser(String email, String password, String name) {
        // Проверка на уникальность email
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return Optional.empty();
        }

        User user = new User(email, password, name);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> loginUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        return Optional.empty();
    }

    @Override
    public boolean updateUser(int userId, String newEmail, String newPassword, String newName) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) return false;

        Optional<User> emailUser = userRepository.findByEmail(newEmail);
        if (emailUser.isPresent() && emailUser.get().getId() != userId) {
            return false;
        }

        User user = userOptional.get();
        user.setEmail(newEmail);
        user.setPassword(newPassword);
        user.setName(newName);

        return userRepository.update(user);
    }

    @Override
    public boolean deleteUser(int userId) {
        return userRepository.delete(userId);
    }
}
