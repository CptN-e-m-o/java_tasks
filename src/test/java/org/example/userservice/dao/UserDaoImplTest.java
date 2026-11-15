
package org.example.userservice.dao;

import org.example.userservice.exception.DuplicateEmailException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.example.userservice.entity.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoImplTest {

    private static SessionFactory sessionFactory;
    private UserDao userDao;

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("user_service_test")
                    .withUsername("test")
                    .withPassword("test");

    @Test
    void postgresContainer_starts() {
        assertTrue(postgres.isRunning());
    }


    @BeforeAll
    static void setUpAll() {
        sessionFactory = new Configuration()
                .addAnnotatedClass(User.class)
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.url", postgres.getJdbcUrl())
                .setProperty("hibernate.connection.username", postgres.getUsername())
                .setProperty("hibernate.connection.password", postgres.getPassword())
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                session.createMutationQuery("delete from User")
                        .executeUpdate();

                transaction.commit();
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }

        userDao = new UserDaoImpl(sessionFactory);
    }

    @AfterAll
    static void tearDownAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    void create_whenValidUser_savesToDatabase() {
        User user = new User("User", "user@test.com", 25);
        userDao.create(user);

        assertNotNull(user.getId());

        Optional<User> result = userDao.findById(user.getId());

        assertTrue(result.isPresent());

        User savedUser = result.get();
        assertEquals("User", savedUser.getName());
        assertEquals("user@test.com", savedUser.getEmail());
        assertEquals(25, savedUser.getAge());
    }

    @Test
    void findById_whenUserNotFound_returnsEmpty() {
        Long id = 999L;

        assertTrue(userDao.findById(id).isEmpty());
    }

    @Test
    void update_whenValidUser_updatesDatabase() {
        User user = new User("User", "user@test.com", 25);
        userDao.create(user);

        user.setName("User2");
        userDao.update(user);

        Optional<User> result = userDao.findById(user.getId());

        assertTrue(result.isPresent());
        assertEquals("User2", result.get().getName());
    }

    @Test
    void delete_whenUserExists_removesFromDatabase() {
        User user = new User("User", "user@test.com", 25);
        userDao.create(user);

        Long userId = user.getId();

        userDao.delete(userId);
        Optional<User> result = userDao.findById(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_whenUsersExist_returnsAllUsers() {
        User user1 = new User("user1", "user1@test.com", 20);
        User user2 = new User("user2", "user2@test.com", 20);

        userDao.create(user1);
        userDao.create(user2);

        List<User> result = userDao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .anyMatch(user -> user.getEmail().equals(user1.getEmail())));

        assertTrue(result.stream()
                .anyMatch(user -> user.getEmail().equals(user2.getEmail())));
    }

    @Test
    void create_whenEmailAlreadyExists_throwsException() {
        User user1 = new User("user1", "user@test.com", 20);
        User user2 = new User("user2", "user@test.com", 20);

        userDao.create(user1);
        assertThrows(DuplicateEmailException.class,
                () -> userDao.create(user2));

        List<User> result = userDao.findAll();
        assertEquals(1, result.size());
    }
}
