package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "User data returned by API")
public record UserResponse (
        @Schema(
                description = "Уникальный идентификатор пользователя, назначается базой данных",
                example = "5"
        )
        Long id,

        @Schema(description = "Имя пользователя", example = "Ivan")
        String name,

        @Schema(description = "Email пользователя", example = "ivan@example.com")
        String email,

        @Schema(description = "Возраст пользователя в годах", example = "23")
        int age,

        @Schema(
                description = "Дата и время создания пользователя в UTC, назначаются сервером",
                example = "2026-10-24T12:00:00Z"
        )
        Instant createdAt
){
}
