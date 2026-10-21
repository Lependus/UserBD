package org.example.notification.service;

import org.example.notification.event.UserEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNotification(UserEvent event){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.email());
        message.setFrom("noreply@example.com");
        message.setSubject("Уведомление об аккаунте");

        switch (event.operation()){
            case CREATED -> message.setText("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
            case DELETED -> message.setText("Здравствуйте! Ваш аккаунт был удалён.");
        }

        mailSender.send(message);
    }

}
