
package org.example.userservice.controller;

import jakarta.validation.Valid;
import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.entity.User;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return users.stream()
                .map(UserMapper::toResponse)
                .toList();
    }


    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        Optional<User> result = userService.getUserById(id);

        User user = result.orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                )
        );

        return UserMapper.toResponse(user);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        User user = userService.createUser(request.name(), request.email(), request.age());
        return UserMapper.toResponse(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {
        User user = userService.updateUser(
                id,
                request.name(),
                request.email(),
                request.age()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Пользователь не найден"
        ));

        return UserMapper.toResponse(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        if (!userService.deleteUser(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }
    }
}
