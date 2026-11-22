
package org.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotBlank(message = "Имя пользователя обязательно")
        String name,

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
                String email,

        @NotNull(message = "Возраст обязателен")
        @Min(value = 0, message = "Возраст не может быть отрицательным")
        @Max(value = 120, message = "Возраст не может превышать 120 лет")
        Integer age
) {
}
