package ru.habittracker.repository;

public class SqlConstants {
    // Запросы для HabitRecord
    public static final String INSERT_HABIT_RECORD = "INSERT INTO habit_records (id, habit_id, date, completed) VALUES (nextval('habit_record_seq'), ?, ?, ?) RETURNING id";
    public static final String SELECT_HABIT_RECORD_BY_ID = "SELECT id, habit_id, date, completed FROM habit_records WHERE id = ?";
    public static final String SELECT_HABIT_RECORDS_BY_HABIT_ID = "SELECT id, habit_id, date, completed FROM habit_records WHERE habit_id = ?";
    public static final String SELECT_HABIT_RECORDS_BY_USER_ID_AND_DATE = "SELECT hr.id, hr.habit_id, hr.date, hr.completed " +
            "FROM habit_records hr " +
            "JOIN habits h ON hr.habit_id = h.id " +
            "WHERE h.user_id = ? AND hr.date = ?";
    public static final String DELETE_HABIT_RECORD_BY_ID = "DELETE FROM habit_records WHERE id = ?";

    // Запросы для Habit
    public static final String INSERT_HABIT = "INSERT INTO habits (id, title, description, frequency, user_id, creation_date) " +
            "VALUES (nextval('habit_seq'), ?, ?, ?, ?, ?) RETURNING id";
    public static final String SELECT_HABIT_BY_ID_AND_USER_ID = "SELECT id, title, description, frequency, user_id, creation_date FROM habits WHERE id = ? AND user_id = ?";
    public static final String SELECT_HABITS_BY_USER_ID = "SELECT id, title, description, frequency, user_id, creation_date FROM habits WHERE user_id = ?";
    public static final String SELECT_HABITS_BY_USER_ID_AND_CREATION_DATE = "SELECT id, title, description, frequency, user_id, creation_date FROM habits WHERE user_id = ? AND creation_date = ?";
    public static final String SELECT_HABITS_BY_USER_ID_AND_FREQUENCY = "SELECT id, title, description, frequency, user_id, creation_date FROM habits WHERE user_id = ? AND frequency = ?";
    public static final String UPDATE_HABIT = "UPDATE habits SET title = ?, description = ?, frequency = ? WHERE id = ? AND user_id = ?";
    public static final String DELETE_HABIT_BY_ID_AND_USER_ID = "DELETE FROM habits WHERE id = ? AND user_id = ?";

    // Запросы для User
    public static final String INSERT_USER = "INSERT INTO users (id, email, password, name) VALUES (nextval('user_seq'), ?, ?, ?) RETURNING id";
    public static final String SELECT_USER_BY_EMAIL = "SELECT id, email, password, name FROM users WHERE email = ?";
    public static final String SELECT_USER_BY_ID = "SELECT id, email, password, name FROM users WHERE id = ?";
    public static final String UPDATE_USER = "UPDATE users SET email = ?, password = ?, name = ? WHERE id = ?";
    public static final String DELETE_USER_BY_ID = "DELETE FROM users WHERE id = ?";
}
