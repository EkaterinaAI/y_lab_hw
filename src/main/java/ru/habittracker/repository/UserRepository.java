package ru.habittracker.repository;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.User;
import ru.habittracker.repository.interfaces.IUserRepository;

import java.sql.*;
import java.util.Optional;

/**
 * Репозиторий для работы с таблицей "users" в базе данных.
 * <p>
 * Предоставляет методы для сохранения, обновления, удаления и поиска пользователей.
 * </p>
 *
 * author
 *     Ekaterina Ishchuk
 */
public class UserRepository implements IUserRepository {

    private final DatabaseConnectionManager dbManager;

    /**
     * Конструктор репозитория пользователей.
     *
     * @param dbManager менеджер подключения к базе данных
     */
    public UserRepository(DatabaseConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Сохраняет нового пользователя в базе данных.
     *
     * @param user объект пользователя для сохранения
     * @return сохранённый объект пользователя с установленным ID
     */
    @Override
    public Optional<User> save(User user) {
        String sql = "INSERT INTO users (id, email, password, name) VALUES (nextval('user_seq'), ?, ?, ?) RETURNING id";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getName());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                user.setId(id);
                return Optional.of(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Находит пользователя по email.
     *
     * @param email email пользователя
     * @return объект пользователя или пустой Optional, если не найдено
     */
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, email, password, name FROM users WHERE email = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("name")
                );
                return Optional.of(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Находит пользователя по ID.
     *
     * @param userId ID пользователя
     * @return объект пользователя или пустой Optional, если не найдено
     */
    @Override
    public Optional<User> findById(int userId) {
        String sql = "SELECT id, email, password, name FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("name")
                );
                return Optional.of(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Обновляет информацию о пользователе.
     *
     * @param user объект пользователя с обновлёнными данными
     * @return true, если обновление прошло успешно
     */
    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET email = ?, password = ?, name = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getName());
            stmt.setInt(4, user.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @param userId ID пользователя
     * @return true, если удаление прошло успешно
     */
    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
