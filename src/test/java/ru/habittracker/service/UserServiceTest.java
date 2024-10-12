package ru.habittracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.habittracker.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        Optional<User> user = userService.registerUser("test@example.com", "password", "Test User");
        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldNotRegisterUserWithDuplicateEmail() {
        userService.registerUser("duplicate@example.com", "password", "Test User");
        Optional<User> duplicateUser = userService.registerUser("duplicate@example.com", "password", "Test User");

        assertThat(duplicateUser).isEmpty();
    }

    @Test
    void shouldLoginUserSuccessfully() {
        userService.registerUser("login@example.com", "password", "Login User");
        Optional<User> user = userService.loginUser("login@example.com", "password");

        assertThat(user).isPresent();
        assertThat(user.get().getName()).isEqualTo("Login User");
    }

    @Test
    void shouldNotLoginWithWrongPassword() {
        userService.registerUser("test@example.com", "password", "Test User");
        Optional<User> user = userService.loginUser("test@example.com", "wrongpassword");

        assertThat(user).isEmpty();
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        Optional<User> user = userService.registerUser("update@example.com", "password", "Update User");
        assertTrue(user.isPresent());

        boolean isUpdated = userService.updateUser(user.get().getId(), "newemail@example.com", "newpassword", "New Name");
        assertTrue(isUpdated);

        Optional<User> updatedUser = userService.loginUser("newemail@example.com", "newpassword");
        assertTrue(updatedUser.isPresent());
        assertThat(updatedUser.get().getName()).isEqualTo("New Name");
    }

    @Test
    void shouldNotUpdateNonexistentUser() {
        boolean isUpdated = userService.updateUser(999, "newemail@example.com", "newpassword", "New Name");
        assertFalse(isUpdated);
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        Optional<User> user = userService.registerUser("delete@example.com", "password", "Delete User");
        assertTrue(user.isPresent());

        boolean isDeleted = userService.deleteUser(user.get().getId());
        assertTrue(isDeleted);

        Optional<User> deletedUser = userService.loginUser("delete@example.com", "password");
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void shouldNotDeleteNonexistentUser() {
        boolean isDeleted = userService.deleteUser(999);
        assertFalse(isDeleted);
    }
}
