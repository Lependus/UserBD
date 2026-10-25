package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API error message")
public record ErrorResponse (
        @Schema(description = "Error reason",
                example = "Данные нарушают ограничения базы данных. Проверьте уникальность email")
        String message) {
}
