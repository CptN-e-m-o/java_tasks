
package org.example.userservice.controller;

import org.example.userservice.entity.User;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;


    @Test
    void getAllUsers_returnsUsers() throws Exception {
        User user = new User("Ivan", "ivan@test.com", 25);

        when(userService.getAllUsers())
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ivan"))
                .andExpect(jsonPath("$[0].email").value("ivan@test.com"))
                .andExpect(jsonPath("$[0].age").value(25));

        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_whenUserExists_returnsUser() throws Exception {
        Long id = 1L;
        User user = new User("Ivan", "ivan@test.com", 25);

        when(userService.getUserById(id))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.email").value("ivan@test.com"));

        verify(userService).getUserById(id);
    }

    @Test
    void getUserById_whenUserNotFound_returns404() throws Exception {
        Long id = 999L;

        when(userService.getUserById(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Пользователь не найден"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(id);
    }


    @Test
    void createUser_withValidData_returns201() throws Exception {
        User user = new User("Ivan", "ivan@test.com", 25);

        when(userService.createUser("Ivan", "ivan@test.com", 25))
                .thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Ivan",
                                "email": "ivan@test.com",
                                "age": 25
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@test.com"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService).createUser("Ivan", "ivan@test.com", 25);
    }

    @Test
    void createUser_withInvalidAge_returns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Ivan",
                                "email": "ivan@test.com",
                                "age": -10
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Возраст не может быть отрицательным"));

        verifyNoInteractions(userService);
    }


    @Test
    void updateUser_whenUserExists_returns200() throws Exception {
        User user = new User("Petr", "petr@test.com", 30);

        when(userService.updateUser(1L, "Petr", "petr@test.com", 30))
                .thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Petr",
                                "email": "petr@test.com",
                                "age": 30
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Petr"))
                .andExpect(jsonPath("$.email").value("petr@test.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService).updateUser(1L, "Petr", "petr@test.com", 30);
    }

    @Test
    void updateUser_whenUserNotFound_returns404() throws Exception {
        when(userService.updateUser(999L, "Petr", "petr@test.com", 30))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Petr",
                                "email": "petr@test.com",
                                "age": 30
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Пользователь не найден"));

        verify(userService).updateUser(999L, "Petr", "petr@test.com", 30);
    }

    @Test
    void deleteUser_whenUserExists_returns204() throws Exception {
        when(userService.deleteUser(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_whenUserNotFound_returns404() throws Exception {
        when(userService.deleteUser(999L))
                .thenReturn(false);

        mockMvc.perform(delete("/api/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Пользователь не найден"));

        verify(userService).deleteUser(999L);
    }


    @Test
    void createUser_withDuplicateEmail_returns409() throws Exception {
        when(userService.createUser("Ivan", "ivan@test.com", 25))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Ivan",
                                "email": "ivan@test.com",
                                "age": 25
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Нарушение ограничений данных"));

        verify(userService).createUser("Ivan", "ivan@test.com", 25);
    }

}
