package org.example.exception;

import org.example.dto.ErrorResponse;
import org.example.dto.ValidationErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.putIfAbsent(
                                error.getField(),
                            error.getDefaultMessage()
                    )
            );

        ValidationErrorResponse response = new ValidationErrorResponse("Ошибка проверки данных", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrityViolationException (DataIntegrityViolationException exception){
        ErrorResponse errorResponse = new ErrorResponse("Данные нарушают ограничения базы данных. Проверьте уникальность email");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
