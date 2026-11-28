package org.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        String password = System.getenv("DB_PASSWORD");

        System.out.println(
                "DB_PASSWORD доступна: " + (password != null && !password.isBlank())
        );

        SpringApplication.run(UserServiceApplication.class, args);
    }
}