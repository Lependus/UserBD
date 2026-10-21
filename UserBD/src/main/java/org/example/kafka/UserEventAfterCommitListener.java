package org.example.kafka;

import org.example.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserEventAfterCommitListener {
    private static final Logger log = LoggerFactory.getLogger(UserEventAfterCommitListener.class);
    private final UserEventProducer producer;

    public UserEventAfterCommitListener(UserEventProducer producer) {
        this.producer = producer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserEvent event){
        try
        {
            producer.publish(event).whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Не удалось отправить событие пользователя в Kafka", exception);
                }

            });
        }
        catch (Exception exception){
            log.error("Не удалось отправить событие пользователя в Kafka", exception);
        }
    }
}
