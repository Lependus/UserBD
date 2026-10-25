package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Users data for create and update")
public record UserRequest (
        @Schema(description = "Имя пользователя, не должно быть пустым, максимум 100 символов",
        example = "Ivan")
        @NotBlank(message = "Имя не должно быть пустым.")
        @Size(max = 100, message = "Имя не должно превышать 100 символов.")
        String name,

        @Schema(description = "Корректный email, максимум 254 символа; должен быть уникальным среди пользователей",
        example = "ivan@example.com")
        @NotBlank(message = "Email не должен быть пустым.")
        @Email(message = "Некорректный Email.")
        @Pattern(regexp= "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Некорректный Email.")
        @Size(max = 254, message = "Email не должен превышать 254 символа.")
        String email,

        @Schema(description = "Возраст пользователя от 0 до 150 включительно",
        example = "23")
        @NotNull(message = "Возраст не должен быть пустым.")
        @Min(value = 0, message = "Возраст не должен быть отрицательным.")
        @Max(value = 150, message = "Возраст не должен быть больше 150.")
        Integer age) {
}