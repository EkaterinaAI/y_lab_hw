package ru.habittracker.repository;

import ru.habittracker.model.User;

import java.util.Optional;

/**
 * Интерфейс для репозитория пользователей.
 * <p>
 * Определяет методы для сохранения, обновления, удаления и поиска пользователей.
 * </p>
 * <p>
 * Связанные классы:
 * <ul>
 *     <li>{@link User}</li>
 * </ul>
 * </p>
 *
 * author
 *     Ekaterina Ishchuk
 */
public interface IUserRepository {
    /**
     * Сохраняет нового пользователя.
     *
     * @param user объект пользователя для сохранения
     * @return сохранённый объект пользователя с установленным ID
     */
    Optional<User> save(User user);

    /**
     * Находит пользователя по email.
     *
     * @param email email пользователя
     * @return объект пользователя или пустой Optional, если не найдено
     */
    Optional<User> findByEmail(String email);

    /**
     * Находит пользователя по ID.
     *
     * @param userId ID пользователя
     * @return объект пользователя или пустой Optional, если не найдено
     */
    Optional<User> findById(int userId);

    /**
     * Обновляет информацию о пользователе.
     *
     * @param user объект пользователя с обновлёнными данными
     * @return true, если обновление прошло успешно
     */
    boolean update(User user);

    /**
     * Удаляет пользователя по ID.
     *
     * @param userId ID пользователя
     * @return true, если удаление прошло успешно
     */
    boolean delete(int userId);
}
