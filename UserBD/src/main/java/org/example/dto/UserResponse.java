package org.example.dto;

import java.time.Instant;

public record UserResponse (
        Long id,
        String name,
        String email,
        int age,
        Instant createdAt
){
}
