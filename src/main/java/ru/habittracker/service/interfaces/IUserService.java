package ru.habittracker.service.interfaces;

import ru.habittracker.model.User;

import java.util.Optional;

/**
 * Интерфейс для сервиса управления пользователями.
 * <p>
 * Определяет методы для регистрации, входа, обновления и удаления пользователей.
 * </p>
 *
 * author
 *     Ekaterina Ishchuk
 */
public interface IUserService {
    Optional<User> registerUser(String email, String password, String name);
    Optional<User> loginUser(String email, String password);
    boolean updateUser(int userId, String newEmail, String newPassword, String newName);
    boolean deleteUser(int userId);
}
