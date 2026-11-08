package org.example.userservice.service;

import org.example.userservice.dao.UserDao;
import org.example.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void createUser(String name, String email, int age) {
        validateUserData(name, email, age);
        User user = new User(name, email, age);

        userDao.create(user);
    }

    public Optional<User> getUserById(Long id) {
        validateUserId(id);
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public void updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User равен null");
        }
        validateUserId(user.getId());
        validateUserData(user.getName(), user.getEmail(), user.getAge());

        userDao.update(user);
    }

    public void deleteUser(Long id) {
        validateUserId(id);
        userDao.delete(id);
    }

    private void validateUserData(String name, String email, Integer  age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        } else if (email == null || email.isBlank() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Некорректный email");
        } else if (age == null || age < 0 || age > 120) {
            throw new IllegalArgumentException("Некорректный возраст");
        }
    }

    private void validateUserId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Некорректный ID пользователя");
        }
    }
}