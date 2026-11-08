
package org.example.userservice.console;

import org.example.userservice.entity.User;
import org.example.userservice.exception.DuplicateEmailException;
import org.example.userservice.service.UserService;

import java.util.List;
import java.util.Scanner;

import jakarta.persistence.PersistenceException;
import org.hibernate.exception.JDBCConnectionException;

public class ConsoleMenu {

    private final UserService userService;
    private final Scanner scanner;

    public ConsoleMenu(UserService userService) {
        this.userService = userService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        boolean running = true;

        while (running) {
            System.out.println("\n========== USER SERVICE ==========");
            System.out.println("1. Создать пользователя");
            System.out.println("2. Найти пользователя по ID");
            System.out.println("3. Показать всех пользователей");
            System.out.println("4. Обновить пользователя");
            System.out.println("5. Удалить пользователя");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            String input = scanner.nextLine();

            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Введите число от 0 до 5!");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> createUser();
                    case 2 -> findUserById();
                    case 3 -> showAllUsers();
                    case 4 -> updateUser();
                    case 5 -> deleteUser();
                    case 0 -> running = false;
                    default -> System.out.println("Неизвестная команда");
                }

            } catch (JDBCConnectionException e) {
                System.out.println(
                        "Не удалось связаться с PostgreSQL. Проверьте подключение к базе данных."
                );

            } catch (PersistenceException e) {
                System.out.println(
                        "Произошла ошибка при работе с базой данных."
                );
            }
        }

        System.out.println("Приложение завершено.");
    }

    private void createUser() {
        System.out.println("Создание пользователя");
        System.out.println("Шаг 1. Введите имя");
        String name = scanner.nextLine();
        System.out.println("Шаг 2. Введите email");
        String email = scanner.nextLine();
        System.out.println("Шаг 3. Введите возраст");
        String ageInput = scanner.nextLine();
        try {
            int age = Integer.parseInt(ageInput);

            userService.createUser(name, email, age);

            System.out.println("Пользователь успешно создан!");

        } catch (NumberFormatException e) {
            System.out.println("Возраст должен быть числом");

        } catch (IllegalArgumentException | DuplicateEmailException e) {
            System.out.println(e.getMessage());
        }
    }

    private void findUserById() {
        System.out.println("Поиск пользователя");
        System.out.println("Введите ID пользователя");
        String idInput = scanner.nextLine();


        try {
            Long id = Long.parseLong(idInput);

            userService.getUserById(id).ifPresentOrElse(
                    System.out::println,
                    () -> System.out.println("Пользователь не найден")
            );

        } catch (NumberFormatException e) {
            System.out.println("Id должен быть числом");

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void showAllUsers() {
        System.out.println("Список пользователей");

        List<User> userList = userService.getAllUsers();
        if (userList.isEmpty()) {

            System.out.println("Пользователей пока нет");
        } else {
            userList.forEach(System.out::println);
        }
    }

    private void updateUser() {
        System.out.println("Обновление пользователя");
        System.out.println("Введите ID пользователя");
        String idInput = scanner.nextLine();

        try {
            Long id = Long.parseLong(idInput);

            User user = userService.getUserById(id).orElse(null);

            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }

            System.out.println("Введите имя пользователя");
            String name = scanner.nextLine();
            System.out.println("Введите email пользователя");
            String email = scanner.nextLine();
            System.out.println("Шаг 3. Введите возраст");
            String ageInput = scanner.nextLine();

            try {
                int age = Integer.parseInt(ageInput);

                user.setName(name);
                user.setEmail(email);
                user.setAge(age);

                userService.updateUser(user);

                System.out.println("Пользователь успешно обновлен!");
                System.out.println(user);

            } catch (NumberFormatException e) {
                System.out.println("Возраст должен быть числом");

            }

        } catch (NumberFormatException e) {
            System.out.println("Id должен быть числом");

        } catch (IllegalArgumentException | DuplicateEmailException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteUser() {
        System.out.println("Удаление пользователя");
        System.out.println("Введите ID пользователя");
        String idInput = scanner.nextLine();

        try {
            Long id = Long.parseLong(idInput);

            User user = userService.getUserById(id).orElse(null);

            if (user == null) {
                System.out.println("Пользователь не найден");
                return;
            }

            userService.deleteUser(id);

            System.out.println("Пользователь удалён");

        } catch (NumberFormatException e) {
            System.out.println("Id должен быть числом");

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
