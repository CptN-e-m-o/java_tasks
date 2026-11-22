
package org.example.userservice.repository;

import jakarta.persistence.EntityManager;
import org.example.userservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("user_service_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveUser_thenFindById_returnsUser() {
        User user = new User("Ivan", "ivan@test.com", 25);

        User savedUser = userRepository.saveAndFlush(user);
        Long id = savedUser.getId();

        entityManager.clear();

        Optional<User> foundUser = userRepository.findById(id);

        assertNotNull(id);
        assertTrue(foundUser.isPresent());
        assertEquals("Ivan", foundUser.get().getName());
        assertEquals("ivan@test.com", foundUser.get().getEmail());
        assertEquals(25, foundUser.get().getAge());
    }

    @Test
    void findById_whenUserNotFound_returnsEmpty() {
        Optional<User> foundUser = userRepository.findById(999L);

        assertTrue(foundUser.isEmpty());
    }

    @Test
    void findAll_returnsUsers() {
        User firstUser = new User("Ivan", "ivan@test.com", 25);
        User secondUser = new User("Petr", "petr@test.com", 30);

        userRepository.saveAndFlush(firstUser);
        userRepository.saveAndFlush(secondUser);

        entityManager.clear();

        List<User> users = userRepository.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream()
                .anyMatch(user -> user.getEmail().equals("ivan@test.com")));
        assertTrue(users.stream()
                .anyMatch(user -> user.getEmail().equals("petr@test.com")));
    }

    @Test
    void updateUser_whenUserExists_updatesData() {
        User user = new User("Ivan", "ivan@test.com", 25);

        User savedUser = userRepository.saveAndFlush(user);
        Long id = savedUser.getId();

        savedUser.setName("Petr");
        savedUser.setEmail("petr@test.com");
        savedUser.setAge(30);

        userRepository.saveAndFlush(savedUser);

        entityManager.clear();

        User updatedUser = userRepository.findById(id).orElseThrow();

        assertEquals("Petr", updatedUser.getName());
        assertEquals("petr@test.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());
    }

    @Test
    void deleteUser_whenUserExists_removesUser() {
        User user = new User("Ivan", "ivan@test.com", 25);

        User savedUser = userRepository.saveAndFlush(user);
        Long id = savedUser.getId();

        userRepository.deleteById(id);
        userRepository.flush();

        entityManager.clear();

        Optional<User> deletedUser = userRepository.findById(id);

        assertTrue(deletedUser.isEmpty());
    }

    @Test
    void saveUser_withDuplicateEmail_throwsException() {
        User firstUser = new User("Ivan", "ivan@test.com", 25);
        User secondUser = new User("Petr", "ivan@test.com", 30);

        userRepository.saveAndFlush(firstUser);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(secondUser)
        );
    }
}