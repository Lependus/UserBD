package org.example.kafka;

import org.example.event.UserEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UserEventProducer {
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, UserEvent>> publish(UserEvent event){
        return kafkaTemplate.send("user-events", event.email(), event);
    }
}
