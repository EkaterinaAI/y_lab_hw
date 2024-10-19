package ru.habittracker.repository;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.HabitRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HabitRecordRepository {

    private final DatabaseConnectionManager dbManager;

    // Конструктор для передачи DatabaseConnectionManager
    public HabitRecordRepository(DatabaseConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    // Сохранение записи привычки
    public Optional<HabitRecord> save(HabitRecord record) {
        String sql = "INSERT INTO habit_records (id, habit_id, date, completed) VALUES (nextval('habit_record_seq'), ?, ?, ?) RETURNING id";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, record.getHabitId());
            stmt.setDate(2, Date.valueOf(record.getDate()));
            stmt.setBoolean(3, record.isCompleted());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                // Assuming HabitRecord has a setter for id if needed
                // record.setId(id); // Uncomment if HabitRecord has an id field
                return Optional.of(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Поиск записей привычки по habitId
    public List<HabitRecord> findByHabitId(int habitId) {
        String sql = "SELECT habit_id, date, completed FROM habit_records WHERE habit_id = ?";
        List<HabitRecord> records = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, habitId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HabitRecord record = new HabitRecord(
                        rs.getInt("habit_id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getBoolean("completed")
                );
                records.add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // Дополнительные методы для тестов
    public Optional<HabitRecord> findById(int id) {
        String sql = "SELECT habit_id, date, completed FROM habit_records WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                HabitRecord record = new HabitRecord(
                        rs.getInt("habit_id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getBoolean("completed")
                );
                return Optional.of(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM habit_records WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Метод для поиска записей по пользователю и дате
    public List<HabitRecord> findByUserIdAndDate(int userId, LocalDate date) {
        String sql = "SELECT hr.habit_id, hr.date, hr.completed " +
                "FROM habit_records hr " +
                "JOIN habits h ON hr.habit_id = h.id " +
                "WHERE h.user_id = ? AND hr.date = ?";
        List<HabitRecord> records = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HabitRecord record = new HabitRecord(
                        rs.getInt("habit_id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getBoolean("completed")
                );
                records.add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}
