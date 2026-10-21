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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.mail.host=127.0.0.1",
        "spring.mail.port=3025",
        "spring.kafka.listener.auto-startup=false"})
@AutoConfigureMockMvc
public class NotificationControllerIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;


    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(
            new ServerSetup(3025, "127.0.0.1", "smtp"
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

    @Test
    void shouldSendCreated() throws Exception {
        UserEvent event = new UserEvent(UserOperation.CREATED, "test@example.com");

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isNoContent());

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

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

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isNoContent());

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage[] messages = greenMail.getReceivedMessages();

        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertEquals(event.email(), message.getAllRecipients()[0].toString());
        assertEquals("noreply@example.com", message.getFrom()[0].toString());
        assertEquals("Уведомление об аккаунте", message.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", ((String) message.getContent()).trim());

    }

    @Test
    void shouldRejectBlankEmail() throws Exception {
        UserEvent event = new UserEvent(UserOperation.DELETED, "");

        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest());

        assertEquals(0, greenMail.getReceivedMessages().length);

    }
}
