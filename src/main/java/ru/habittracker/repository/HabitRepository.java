package ru.habittracker.repository;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.Habit;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitRepository {

    private final DatabaseConnectionManager dbManager;

    // Конструктор для передачи DatabaseConnectionManager
    public HabitRepository(DatabaseConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    public Habit save(Habit habit) {
        String sql = "INSERT INTO habits (id, title, description, frequency, user_id, creation_date) " +
                "VALUES (nextval('habit_seq'), ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, habit.getTitle());
            stmt.setString(2, habit.getDescription());
            stmt.setInt(3, habit.getFrequency());
            stmt.setInt(4, habit.getUserId());
            stmt.setDate(5, Date.valueOf(habit.getCreationDate()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                habit.setId(id);
                return habit;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Habit findByIdAndUserId(int id, int userId) {
        String sql = "SELECT id, title, description, frequency, user_id, creation_date FROM habits WHERE id = ? AND user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Habit(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("frequency"),
                        rs.getInt("user_id"),
                        rs.getDate("creation_date").toLocalDate()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Habit> findByUserId(int userId) {
        String sql = "SELECT id, title, description, frequency, user_id, creation_date FROM habits WHERE user_id = ?";
        List<Habit> habits = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Habit habit = new Habit(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("frequency"),
                        rs.getInt("user_id"),
                        rs.getDate("creation_date").toLocalDate()
                );
                habits.add(habit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    public List<Habit> findByUserIdAndCreationDate(int userId, LocalDate date) {
        String sql = "SELECT id, title, description, frequency, user_id, creation_date FROM habits WHERE user_id = ? AND creation_date = ?";
        List<Habit> habits = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Habit habit = new Habit(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("frequency"),
                        rs.getInt("user_id"),
                        rs.getDate("creation_date").toLocalDate()
                );
                habits.add(habit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    public List<Habit> findByUserIdAndFrequency(int userId, int frequency) {
        String sql = "SELECT id, title, description, frequency, user_id, creation_date FROM habits WHERE user_id = ? AND frequency = ?";
        List<Habit> habits = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, frequency);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Habit habit = new Habit(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("frequency"),
                        rs.getInt("user_id"),
                        rs.getDate("creation_date").toLocalDate()
                );
                habits.add(habit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    public boolean update(Habit habit) {
        String sql = "UPDATE habits SET title = ?, description = ?, frequency = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, habit.getTitle());
            stmt.setString(2, habit.getDescription());
            stmt.setInt(3, habit.getFrequency());
            stmt.setInt(4, habit.getId());
            stmt.setInt(5, habit.getUserId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteByIdAndUserId(int id, int userId) {
        String sql = "DELETE FROM habits WHERE id = ? AND user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, userId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
