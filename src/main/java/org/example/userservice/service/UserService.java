package org.example.userservice.service;

import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String name, String email, int age) {
        validateUserData(name, email, age);

        User user = new User(name, email, age);

        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        validateUserId(id);
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> updateUser(
            Long id,
            String name,
            String email,
            Integer age
    ) {
        validateUserId(id);
        validateUserData(name, email, age);
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            User existingUser = user.get();

            existingUser.setName(name);
            existingUser.setEmail(email);
            existingUser.setAge(age);

            return Optional.of(userRepository.save(existingUser));
        }

        return Optional.empty();
    }

    public boolean deleteUser(Long id) {
        validateUserId(id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            userRepository.delete(existingUser);
            return true;
        }
        return false;
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