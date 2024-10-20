package ru.habittracker.repository;

import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.model.HabitRecord;
import ru.habittracker.repository.interfaces.IHabitRecordRepository;

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
 *
 * author
 *     Ekaterina Ishchuk
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

    /**
     * Сохраняет новую запись о выполнении привычки.
     *
     * @param record объект записи для сохранения
     * @return сохранённый объект записи с установленным ID
     */
    @Override
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
                record.setId(id);
                return Optional.of(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Находит запись о выполнении привычки по ID.
     *
     * @param id ID записи
     * @return объект записи или пустой Optional, если не найдено
     */
    @Override
    public Optional<HabitRecord> findById(int id) {
        String sql = "SELECT id, habit_id, date, completed FROM habit_records WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                HabitRecord record = new HabitRecord(
                        rs.getInt("id"),
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

    /**
     * Находит все записи о выполнении определённой привычки.
     *
     * @param habitId ID привычки
     * @return список записей
     */
    @Override
    public List<HabitRecord> findByHabitId(int habitId) {
        String sql = "SELECT id, habit_id, date, completed FROM habit_records WHERE habit_id = ?";
        List<HabitRecord> records = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, habitId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HabitRecord record = new HabitRecord(
                        rs.getInt("id"),
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

    /**
     * Находит записи о выполнении привычек пользователя за определённую дату.
     *
     * @param userId ID пользователя
     * @param date   дата
     * @return список записей
     */
    @Override
    public List<HabitRecord> findByUserIdAndDate(int userId, LocalDate date) {
        String sql = "SELECT hr.id, hr.habit_id, hr.date, hr.completed " +
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
                        rs.getInt("id"),
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

    /**
     * Удаляет запись о выполнении привычки по ID.
     *
     * @param id ID записи
     * @return true, если удаление прошло успешно
     */
    @Override
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
}
