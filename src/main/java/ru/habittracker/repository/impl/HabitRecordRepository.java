package ru.habittracker.repository.impl;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.HabitRecord;
import ru.habittracker.repository.IHabitRecordRepository;
import ru.habittracker.repository.SqlConstants;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с таблицей "habit_records" в базе данных.
 * <p>
 * Предоставляет методы для сохранения, удаления и поиска записей о выполнении привычек.
 * </p>
 * <p>
 * author
 * Ekaterina Ishchuk
 */
public class HabitRecordRepository implements IHabitRecordRepository {

    private final DatabaseConnectionManager dbManager;

    /**
     * Конструктор репозитория записей привычек.
     *
     * @param dbManager менеджер подключения к базе данных
     */
    public HabitRecordRepository(DatabaseConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Optional<HabitRecord> save(HabitRecord record) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.INSERT_HABIT_RECORD)) {

            stmt.setInt(1, record.getHabitId());
            stmt.setDate(2, Date.valueOf(record.getDate()));
            stmt.setBoolean(3, record.isCompleted());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    record.setId(id);
                    return Optional.of(record);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<HabitRecord> findById(int id) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.SELECT_HABIT_RECORD_BY_ID)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    HabitRecord record = new HabitRecord(
                            rs.getInt("id"),
                            rs.getInt("habit_id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getBoolean("completed")
                    );
                    return Optional.of(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<HabitRecord> findByHabitId(int habitId) {
        List<HabitRecord> records = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.SELECT_HABIT_RECORDS_BY_HABIT_ID)) {

            stmt.setInt(1, habitId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HabitRecord record = new HabitRecord(
                            rs.getInt("id"),
                            rs.getInt("habit_id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getBoolean("completed")
                    );
                    records.add(record);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public List<HabitRecord> findByUserIdAndDate(int userId, LocalDate date) {
        List<HabitRecord> records = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.SELECT_HABIT_RECORDS_BY_USER_ID_AND_DATE)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HabitRecord record = new HabitRecord(
                            rs.getInt("id"),
                            rs.getInt("habit_id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getBoolean("completed")
                    );
                    records.add(record);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public boolean delete(int id) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.DELETE_HABIT_RECORD_BY_ID)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
