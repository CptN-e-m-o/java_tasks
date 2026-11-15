
package org.example.userservice.service;

import org.example.userservice.dao.UserDao;
import org.example.userservice.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.util.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_withEmptyName_throwsException() {
        String name = "";
        String email = "test@example.com";
        int age = 25;

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(name, email, age));

        verifyNoInteractions(userDao);
    }

    @Test
    void createUser_withNegativeAge_throwsException() {
        String name = "User";
        String email = "test@example.com";
        int age = -10;

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(name, email, age));

        verifyNoInteractions(userDao);
    }

    @Test
    void createUser_withValidData_callsDao() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        String name = "User";
        String email = "test@example.com";
        int age = 25;

        userService.createUser(name, email, age);
        verify(userDao).create(captor.capture());

        User capturedUser = captor.getValue();

        assertEquals("User", capturedUser.getName());
        assertEquals("test@example.com", capturedUser.getEmail());
        assertEquals(25, capturedUser.getAge());
    }


    @Test
    void getUserById_whenUserExists_returnsUser() {
        Long id = 1L;
        String name = "User";
        String email = "test@example.com";
        int age = 25;

        User user = new User(name, email, age);

        when(userDao.findById(id))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(id);

        assertTrue(result.isPresent());
        assertSame(user, result.get());

        verify(userDao).findById(id);
    }

    @Test
    void getUserById_whenUserNotFound_returnsEmpty() {
        Long id = 999L;

        when(userDao.findById(id))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(id);

        assertTrue(result.isEmpty());
        verify(userDao).findById(id);
    }

    @Test
    void getUserById_withInvalidId_throwsException() {
        Long id = -5L;

        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(id));

        verifyNoInteractions(userDao);
    }

    @Test
    void getAllUsers_whenUsersExist_returnsUsers() {
        User user1 = new User("user1", "user1@test.com", 20);
        User user2 = new User("user2", "user2@test.com", 20);
        List<User> users = new ArrayList<>(Arrays.asList(user1, user2));

        when(userDao.findAll())
                .thenReturn(users);
        assertEquals(users, userService.getAllUsers());
        verify(userDao).findAll();
    }

    @Test
    void getAllUsers_whenNoUsers_returnsEmptyList() {
        when(userDao.findAll()).thenReturn(Collections.emptyList());

        List<User> result = userService.getAllUsers();
        assertTrue(result.isEmpty());

        verify(userDao).findAll();
    }

    @Test
    void updateUser_withValidData_callsDao() throws Exception {
        User user = new User("user", "user@test.com", 25);

        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, 1L);

        userService.updateUser(user);

        verify(userDao).update(user);
    }

    @Test
    void updateUser_withoutId_throwsException() {
        User user = new User("user", "user@test.com", 25);

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(user));

        verifyNoInteractions(userDao);
    }

    @Test
    void updateUser_withNullUser_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(null));

        verifyNoInteractions(userDao);
    }

    @Test
    void deleteUser_withValidId_callsDao() {
        Long id = 1L;

        userService.deleteUser(id);

        verify(userDao).delete(id);
    }

    @Test
    void deleteUser_withInvalidId_throwsException() {
        Long id = -1L;

        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(id));

        verifyNoInteractions(userDao);
    }
}
