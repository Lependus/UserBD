package org.example.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FallBackController {

    @RequestMapping("/fallback/users")
    public ResponseEntity<Map<String, String>> usersFallBack(){
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Сервис пользователей временно недоступен"));
    }

    @RequestMapping("/fallback/notifications")
    public ResponseEntity<Map<String, String>> notificationsFallBack(){
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Сервис уведомлений временно недоступен"));
    }
}
