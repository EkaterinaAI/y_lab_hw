package ru.habittracker.repository.impl;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.Habit;
import ru.habittracker.repository.IHabitRepository;
import ru.habittracker.repository.SqlConstants;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Репозиторий для работы с таблицей "habits" в базе данных.
 * <p>
 * Предоставляет методы для сохранения, обновления, удаления и поиска привычек.
 * </p>
 * <p>
 * author
 * Ekaterina Ishchuk
 */
public class HabitRepository implements IHabitRepository {

    private final DatabaseConnectionManager dbManager;

    /**
     * Конструктор репозитория привычек.
     *
     * @param dbManager менеджер подключения к базе данных
     */
    public HabitRepository(DatabaseConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Habit save(Habit habit) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.INSERT_HABIT)) {

            stmt.setString(1, habit.getTitle());
            stmt.setString(2, habit.getDescription());
            stmt.setInt(3, habit.getFrequency());
            stmt.setInt(4, habit.getUserId());
            stmt.setDate(5, Date.valueOf(habit.getCreationDate()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    habit.setId(id);
                    return habit;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Habit findByIdAndUserId(int id, int userId) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.SELECT_HABIT_BY_ID_AND_USER_ID)) {

            stmt.setInt(1, id);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Habit> findByUserId(int userId) {
        List<Habit> habits = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.SELECT_HABITS_BY_USER_ID)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    @Override
    public List<Habit> findByUserIdAndCreationDate(int userId, LocalDate date) {
        List<Habit> habits = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.SELECT_HABITS_BY_USER_ID_AND_CREATION_DATE)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    @Override
    public List<Habit> findByUserIdAndFrequency(int userId, int frequency) {
        List<Habit> habits = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.SELECT_HABITS_BY_USER_ID_AND_FREQUENCY)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, frequency);

            try (ResultSet rs = stmt.executeQuery()) {
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    @Override
    public boolean update(Habit habit) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.UPDATE_HABIT)) {

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

    @Override
    public boolean deleteByIdAndUserId(int id, int userId) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlConstants.DELETE_HABIT_BY_ID_AND_USER_ID)) {

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
