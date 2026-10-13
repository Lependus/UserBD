package org.example.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern;

public record UserRequest (
        @NotBlank(message = "Имя не должно быть пустым.")
        @Size(max = 100, message = "Имя не должно превышать 100 символов.")
        String name,

        @NotBlank(message = "Email не должен быть пустым.")
        @Email(message = "Некорректный Email.")
        @Pattern(regexp= "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Некорректный Email.")
        @Size(max = 254, message = "Email не должен превышать 254 символа.")
        String email,

        @NotNull(message = "Возраст не должен быть пустым.")
        @Min(value = 0, message = "Возраст не должен быть отрицательным.")
        @Max(value = 150, message = "Возраст не должен быть больше 150.")
        Integer age) {
}