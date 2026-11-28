package org.example.userservice.service;

import org.example.userservice.entity.User;
import org.example.userservice.kafka.UserEventProducer;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    public UserService(
            UserRepository userRepository,
            UserEventProducer userEventProducer
    ) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
    }


    public User createUser(String name, String email, int age) {
        validateUserData(name, email, age);

        User user = userRepository.save(
                new User(name, email, age)
        );

        userEventProducer.userCreated(user.getEmail());

        return user;
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

        if (user.isEmpty()) {
            return false;
        }

        User existingUser = user.get();
        String email = existingUser.getEmail();

        userRepository.delete(existingUser);

        userEventProducer.userDeleted(email);

        return true;
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