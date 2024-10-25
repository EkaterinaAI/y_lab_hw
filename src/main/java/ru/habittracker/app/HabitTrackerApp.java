package ru.habittracker.app;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import ru.habittracker.config.DatabaseConnectionManager;
import ru.habittracker.controller.HabitTrackerController;
import ru.habittracker.service.impl.HabitService;
import ru.habittracker.service.impl.HabitTrackerService;
import ru.habittracker.service.impl.UserService;
import ru.habittracker.service.IHabitService;
import ru.habittracker.service.IHabitTrackerService;
import ru.habittracker.service.IUserService;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// Код класса HabitTrackerApp

public class HabitTrackerApp {

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        System.setErr(new PrintStream(System.err, true, "UTF-8"));

        DatabaseConnectionManager dbManager = new DatabaseConnectionManager();

        // Запуск миграций Liquibase
        runLiquibaseMigrations(dbManager);

        // Инициализация сервисов и контроллера
        IUserService userService = new UserService(dbManager);
        IHabitService habitService = new HabitService(dbManager);
        IHabitTrackerService habitTrackerService = new HabitTrackerService(dbManager);

        HabitTrackerController controller = new HabitTrackerController(userService, habitService, habitTrackerService);
        controller.run();
    }

    private static void runLiquibaseMigrations(DatabaseConnectionManager dbManager) {
        try (Connection connection = dbManager.getConnection();
             Statement statement = connection.createStatement()) {

            // Создание схемы service, если она не существует
            statement.execute("CREATE SCHEMA IF NOT EXISTS service");

            // Настройка и запуск Liquibase миграций
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName("service");

            Liquibase liquibase = new Liquibase("db/changelog/changelog.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());

        } catch (SQLException | LiquibaseException e) {
            e.printStackTrace();
            System.err.println("Ошибка при запуске миграций Liquibase: " + e.getMessage());
        }
    }
}