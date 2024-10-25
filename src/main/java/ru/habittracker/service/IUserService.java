package ru.habittracker.service;

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
    /**
     * Регистрирует нового пользователя.
     *
     * @param email    email пользователя
     * @param password пароль
     * @param name     имя
     * @return объект пользователя, если регистрация успешна
     */
    Optional<User> registerUser(String email, String password, String name);

    /**
     * Выполняет вход пользователя в систему.
     *
     * @param email    email пользователя
     * @param password пароль
     * @return объект пользователя, если вход успешен
     */
    Optional<User> loginUser(String email, String password);

    /**
     * Обновляет данные пользователя.
     *
     * @param userId      ID пользователя
     * @param newEmail    новый email
     * @param newPassword новый пароль
     * @param newName     новое имя
     * @return true, если обновление прошло успешно
     */
    boolean updateUser(int userId, String newEmail, String newPassword, String newName);

    /**
     * Удаляет пользователя.
     *
     * @param userId ID пользователя
     * @return true, если удаление прошло успешно
     */
    boolean deleteUser(int userId);
}
