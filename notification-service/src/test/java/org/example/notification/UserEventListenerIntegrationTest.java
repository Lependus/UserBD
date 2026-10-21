package org.example.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.example.notification.event.UserEvent;
import org.example.notification.event.UserOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {"spring.mail.host=127.0.0.1",
        "spring.mail.port=3026",
        "spring.kafka.listener.auto-startup=true",
        "spring.kafka.consumer.group-id=notification-integration-test",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer"})
@EmbeddedKafka(
        partitions = 1,
        topics = "user-events",
        kraft = true,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext
public class UserEventListenerIntegrationTest {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(
            new ServerSetup(3026, "127.0.0.1", "smtp"
            )).withConfiguration
                    (
                            GreenMailConfiguration.aConfig()
                                    .withUser(
                                            "test@example.com",
                                            "test",
                                            "test"
                                    )
                    )
            .withPerMethodLifecycle(false);
    @BeforeEach
    void clearMessages() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSendCreated() throws Exception {
        UserEvent event = new UserEvent(UserOperation.CREATED, "test@example.com");

        String jsonevent = objectMapper.writeValueAsString(event);

        kafkaTemplate.send("user-events", event.email(), jsonevent)
                .get(10, TimeUnit.SECONDS);

        assertTrue(greenMail.waitForIncomingEmail(10000, 1));

        MimeMessage[] messages = greenMail.getReceivedMessages();

        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertEquals(event.email(), message.getAllRecipients()[0].toString());
        assertEquals("noreply@example.com", message.getFrom()[0].toString());
        assertEquals("Уведомление об аккаунте", message.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.", ((String) message.getContent()).trim());

    }

    @Test
    void shouldSendDeleted() throws Exception {
        UserEvent event = new UserEvent(UserOperation.DELETED, "test@example.com");

        String jsonevent = objectMapper.writeValueAsString(event);

        kafkaTemplate.send("user-events", event.email(), jsonevent)
                .get(10, TimeUnit.SECONDS);

        assertTrue(greenMail.waitForIncomingEmail(10000, 1));

        MimeMessage[] messages = greenMail.getReceivedMessages();

        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertEquals(event.email(), message.getAllRecipients()[0].toString());
        assertEquals("noreply@example.com", message.getFrom()[0].toString());
        assertEquals("Уведомление об аккаунте", message.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", ((String) message.getContent()).trim());

    }
}
