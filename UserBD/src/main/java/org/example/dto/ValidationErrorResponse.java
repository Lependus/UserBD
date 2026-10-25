package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Validation data error response")
public record ValidationErrorResponse (
        @Schema(description = "Error reason",
                example = "Ошибка проверки данных")
        String message,

        @Schema(description = "Error fields: key - field's name, value - error description",
                example = "{\"name\":\"Имя не должно быть пустым.\"}")
        Map<String, String> errors){
}
