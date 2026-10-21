package org.example.notification.kafka;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.example.notification.event.UserEvent;
import org.example.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserEventListener {
    private  final NotificationService service;
    private final Validator validator;


    public UserEventListener(NotificationService service, Validator validator) {
        this.service = service;
        this.validator = validator;
    }

    @KafkaListener(topics = "user-events")
    public void handle(UserEvent event){
        Set<ConstraintViolation<UserEvent>> violations = validator.validate(event);
        if(!violations.isEmpty()){
            throw new ConstraintViolationException("Некорректное событие", violations);
        }
        else service.sendNotification(event);
    }
}
