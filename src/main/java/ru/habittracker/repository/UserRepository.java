package ru.habittracker.repository;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.User;

import java.sql.*;
import java.util.Optional;

public class UserRepository {

    private final DatabaseConnectionManager dbManager;

    // Конструктор для передачи DatabaseConnectionManager
    public UserRepository(DatabaseConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    // Сохранение пользователя
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

    // Поиск пользователя по email
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

    // Поиск пользователя по id
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

    // Обновление данных пользователя
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

    // Удаление пользователя по id
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
