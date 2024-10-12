package ru.habittracker.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static final AtomicInteger userIdCounter = new AtomicInteger(1);
    private static final AtomicInteger habitIdCounter = new AtomicInteger(1);

    // Генерация ID для пользователей
    public static int generateUserId() {
        return userIdCounter.getAndIncrement();
    }

    // Генерация ID для привычек
    public static int generateHabitId() {
        return habitIdCounter.getAndIncrement();
    }
}
