
package org.example.userservice.service;

import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_withEmptyName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("", "test@example.com", 25));

        verifyNoInteractions(userRepository);
    }

    @Test
    void createUser_withNegativeAge_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("User", "test@example.com", -10));

        verifyNoInteractions(userRepository);
    }

    @Test
    void createUser_withValidData_savesUser() {
        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        String name = "User";
        String email = "test@example.com";
        int age = 25;

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(name, email, age);

        verify(userRepository).save(captor.capture());

        User capturedUser = captor.getValue();

        assertEquals(name, capturedUser.getName());
        assertEquals(email, capturedUser.getEmail());
        assertEquals(age, capturedUser.getAge());
        assertSame(capturedUser, result);
    }

    @Test
    void getUserById_whenUserExists_returnsUser() {
        Long id = 1L;
        User user = new User("User", "test@example.com", 25);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(id);

        assertTrue(result.isPresent());
        assertSame(user, result.get());

        verify(userRepository).findById(id);
    }

    @Test
    void getUserById_whenUserNotFound_returnsEmpty() {
        Long id = 999L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(id);

        assertTrue(result.isEmpty());

        verify(userRepository).findById(id);
    }

    @Test
    void getUserById_withInvalidId_throwsException() {
        Long id = -5L;

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(id));

        verifyNoInteractions(userRepository);
    }

    @Test
    void getAllUsers_whenUsersExist_returnsUsers() {
        User user1 = new User("user1", "user1@test.com", 20);
        User user2 = new User("user2", "user2@test.com", 25);

        List<User> users = List.of(user1, user2);

        when(userRepository.findAll())
                .thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(users, result);
        assertEquals(2, result.size());

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_whenNoUsers_returnsEmptyList() {
        when(userRepository.findAll())
                .thenReturn(Collections.emptyList());

        List<User> result = userService.getAllUsers();

        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
    }

    @Test
    void updateUser_withValidData_callsRepository() {
        Long id = 1L;
        User user = new User("Old", "old@test.com", 20);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        Optional<User> result = userService.updateUser(
                id,
                "New",
                "new@test.com",
                30
        );

        assertTrue(result.isPresent());
        assertEquals("New", result.get().getName());
        assertEquals("new@test.com", result.get().getEmail());
        assertEquals(30, result.get().getAge());

        verify(userRepository).findById(id);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_withInvalidId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(
                        null,
                        "User",
                        "user@test.com",
                        25
                ));

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUser_withNullName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(
                        1L,
                        null,
                        "user@test.com",
                        25
                ));

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUser_whenUserNotFound_returnsEmpty() {
        Long id = 999L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.updateUser(
                id,
                "User",
                "user@test.com",
                25
        );

        assertTrue(result.isEmpty());

        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_withValidId_callsRepository() {
        Long id = 1L;
        User user = new User("User", "user@test.com", 25);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        boolean result = userService.deleteUser(id);

        assertTrue(result);

        verify(userRepository).findById(id);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_withInvalidId_throwsException() {
        Long id = -1L;

        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(id));

        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteUser_whenUserNotFound_returnsFalse() {
        Long id = 999L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        boolean result = userService.deleteUser(id);

        assertFalse(result);

        verify(userRepository).findById(id);
        verify(userRepository, never()).delete(any(User.class));
    }
}
