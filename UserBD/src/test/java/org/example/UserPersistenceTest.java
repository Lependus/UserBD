package org.example;

import org.example.event.UserEvent;
import org.example.event.UserOperation;
import org.example.kafka.UserEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserPersistenceTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @MockitoBean
    private UserEventProducer userEventProducer;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeEach
    void clearDatabase() {
        userRepository.deleteAll();
    }
    @BeforeEach
    void publishEvent() {
        when(userEventProducer.publish(any(UserEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
    }
    @Test
    void shouldSaveUser(){
        User user =
                new User(
                        "Alex",
                        "alex@gmail.com",
                        18);

        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        Optional<User> result = userService.findUser(savedUser.getId());

        assertTrue(result.isPresent());

        User createdUser = result.orElseThrow();

        assertNotNull(createdUser.getCreatedAt());
        assertEquals(savedUser.getId(), createdUser.getId());
        assertEquals(savedUser.getName(), createdUser.getName());
        assertEquals(savedUser.getEmail(), createdUser.getEmail());
        assertEquals(savedUser.getAge(), createdUser.getAge());
    }

    @Test
    void shouldReturnUsersList(){
        User user1 = new User(
                "Alex",
                "alex@gmail.com",
                23);
        User user2 = new User(
                "Sasha",
                "sasha@gmail.com",
                14);

        User savedUser1 = userService.saveUser(user1);
        User savedUser2 = userService.saveUser(user2);

        List<User> users = userService.findAllUsers();
        assertEquals(2, users.size());

        Set<Long> actualIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        assertEquals(
                Set.of(savedUser1.getId(), savedUser2.getId()),
                actualIds
        );
    }

    @Test
    void shouldUpdateUser() {
        User savedUser = userService.saveUser(
                new User("Alex", "alex@gmail.com", 18)
        );

        User beforeUpdate = userService.findUser(savedUser.getId())
                .orElseThrow();

        Long originalId = beforeUpdate.getId();
        Instant originalCreatedAt = beforeUpdate.getCreatedAt();
        assertNotNull(originalCreatedAt);

        boolean updated = userService.updateUser(
                originalId, "Sasha", "sasha@gmail.com", 23
        );

        assertTrue(updated);

        User afterUpdate = userService.findUser(originalId)
                .orElseThrow();

        assertEquals("Sasha", afterUpdate.getName());
        assertEquals("sasha@gmail.com", afterUpdate.getEmail());
        assertEquals(23, afterUpdate.getAge());
        assertEquals(originalId, afterUpdate.getId());
        assertEquals(originalCreatedAt, afterUpdate.getCreatedAt());
    }

    @Test
    void shouldDeleteUser() {
        User savedUser = userService.saveUser(
                new User("Alex", "alex@gmail.com", 18)
        );

        Long id = savedUser.getId();

        boolean deleted = userService.deleteUser(id);

        assertTrue(deleted);
        assertTrue(userService.findUser(id).isEmpty());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        User savedUser = userService.saveUser(
                new User("Alex", "alex@gmail.com", 18)
        );

        User secondUser = new User("Sasha", "alex@gmail.com", 23);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.saveUser(secondUser)
        );

        List<User> users = userService.findAllUsers();

        assertEquals(1, users.size());
        assertEquals(savedUser.getId(), users.get(0).getId());
        assertEquals("Alex", users.get(0).getName());
        assertEquals("alex@gmail.com", users.get(0).getEmail());
        assertEquals(18, users.get(0).getAge());
    }

    @Test
    void shouldPublishCreatedOnlyAfterCommit(){
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            userService.saveUser(
                new User(
                        "alex"
        ,               "alex@gmail.com",
                        18
                ));
            verifyNoInteractions(userEventProducer);
        });

        verify(userEventProducer).publish(
                new UserEvent(UserOperation.CREATED, "alex@gmail.com"));

    }

    @Test
    void shouldNotPublishCreatedAfterRollback(){
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            userService.saveUser(
                    new User(
                            "alex"
                            ,               "rollback@example.com",
                            18
                    ));
            verifyNoInteractions(userEventProducer);
            status.setRollbackOnly();
        });

        verifyNoInteractions(userEventProducer);
        assertEquals(0L, userRepository.count());

    }
}
