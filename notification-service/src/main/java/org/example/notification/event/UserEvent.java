package org.example.notification.event;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserEvent(
        @NotNull
        UserOperation operation,
        @NotBlank
        @Email
        String email) {
}
