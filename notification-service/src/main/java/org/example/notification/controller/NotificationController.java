package org.example.notification.controller;

import jakarta.validation.Valid;
import org.example.notification.event.UserEvent;
import org.example.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> post (
            @RequestBody
            @Valid
            UserEvent event){
        service.sendNotification(event);
        return ResponseEntity.noContent().build();
    }
}
