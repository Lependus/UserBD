package org.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
public class UserDaoImplTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    private static SessionFactory factory;
    private static UserDaoImpl userDao;

    @BeforeAll
    static void setUp() {
        Configuration configuration = new Configuration();

        configuration.addAnnotatedClass(User.class);

        configuration.setProperty(
                "hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty(
                "hibernate.connection.username", postgres.getUsername());
        configuration.setProperty(
                "hibernate.connection.password", postgres.getPassword());
        configuration.setProperty(
                "hibernate.connection.driver_class", "org.postgresql.Driver");

        configuration.setProperty(
                "hibernate.hbm2ddl.auto", "create-drop");

        factory = configuration.buildSessionFactory();
        userDao = new UserDaoImpl(factory);
    }

    @BeforeEach
    void clearDatabase() {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                session.createMutationQuery("delete from User")
                        .executeUpdate();
                transaction.commit();
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    void shouldSaveUser(){
        User user = new User("Alex",
                "alex@gmail.com",
                18);

        userDao.save(user);

        assertNotNull(user.getId());

        try (Session session = factory.openSession()){
            User savedUser = session.find(User.class, user.getId());

            assertNotNull(savedUser);
            assertEquals(user.getName(), savedUser.getName());
            assertEquals(user.getEmail(), savedUser.getEmail());
            assertEquals(user.getAge(), savedUser.getAge());
            assertNotNull(savedUser.getCreatedAt());

        }
    }

    @Test
    void shouldReturnUserById(){
        User user = new User("Alex",
                "alex@gmail.com",
                18);
        try (Session session = factory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        }
        Long userID = user.getId();

        Optional<User> result = userDao.findById(userID);

        assertTrue(result.isPresent());

        User foundUser = result.orElseThrow();

        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getName(), foundUser.getName());
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getAge(), foundUser.getAge());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound(){

        Optional<User> result = userDao.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUpdateUser(){
        Long userID;

        try (Session session = factory.openSession()){
            Transaction transaction = session.beginTransaction();

            User user = new User("Alex",
                    "alex@gmail.com",
                    18);

            session.persist(user);
            transaction.commit();

            userID = user.getId();
        }

        boolean updated = userDao.update(
                userID,
                "Sasha",
                "sasha@gmail.com",
                23);

        assertTrue(updated);

        try (Session session = factory.openSession()) {
            User newUser = session.find(User.class, userID);

            assertNotNull(newUser);
            assertEquals("Sasha", newUser.getName());
            assertEquals("sasha@gmail.com", newUser.getEmail());
            assertEquals(23, newUser.getAge());
        }
    }

    @Test
    void shouldFailUpdateWhenUserNotFound(){

        boolean updated = userDao.update(
                1L,
                "Alex",
                "alex@gmail.com",
                18);

        assertFalse(updated);
    }

    @Test
    void shouldDeleteUser(){
        Long userID;

        try (Session session = factory.openSession()){
            Transaction transaction = session.beginTransaction();

            User user = new User("Alex",
                    "alex@gmail.com",
                    18);

            session.persist(user);
            transaction.commit();

            userID = user.getId();
        }

        boolean deleted = userDao.delete(userID);

        assertTrue(deleted);

        try (Session session = factory.openSession()) {
            User newUser = session.find(User.class, userID);

            assertNull(newUser);
        }
    }

    @Test
    void shouldFailDeleteWhenUserNotFound(){

        boolean deleted = userDao.delete(1L);

        assertFalse(deleted);
    }

    @Test
    void shouldFindAllUsers() {
        Long firstUserID;
        Long secondUserID;

        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();

            User user1 = new User("Alex",
                    "alex@gmail.com",
                    18);
            User user2 = new User("Sasha",
                    "sasha@gmail.com",
                    23);

            session.persist(user1);
            session.persist(user2);
            transaction.commit();

            firstUserID = user1.getId();
            secondUserID = user2.getId();
        }

        List<User> users = userDao.findAll();
        assertNotNull(users);

        Set<Long> actualIds = users.stream().map(User::getId).collect(Collectors.toSet());

        assertEquals(2, users.size());
        assertEquals(Set.of(firstUserID, secondUserID), actualIds);
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers(){

        List<User> users = userDao.findAll();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenEmailIncorrect(){
        Long userID;

        try (Session session = factory.openSession()){
            Transaction transaction = session.beginTransaction();

            User user = new User("Alex",
                    "alex@gmail.com",
                    18);

            session.persist(user);
            transaction.commit();

            userID = user.getId();
        }



        assertThrows(IllegalArgumentException.class, () -> {
            userDao.update(
                    userID,
                    "Sasha",
                    "sasha",
                    23);
        });

        try (Session session = factory.openSession()) {
            User newUser = session.find(User.class, userID);

            assertNotNull(newUser);
            assertEquals("Alex", newUser.getName());
            assertEquals("alex@gmail.com", newUser.getEmail());
            assertEquals(18, newUser.getAge());
        }
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists(){
        Long userID;

        try (Session session = factory.openSession()){
            Transaction transaction = session.beginTransaction();

            User user = new User("Alex",
                    "alex@gmail.com",
                    18);

            session.persist(user);
            transaction.commit();

            userID = user.getId();
        }



        assertThrows(org.hibernate.exception.ConstraintViolationException.class, () -> {
            userDao.save(new User(
                    "Sasha",
                    "alex@gmail.com",
                    23));
        });

        try (Session session = factory.openSession()) {
            User newUser = session.find(User.class, userID);

            assertNotNull(newUser);
            assertEquals("Alex", newUser.getName());
            assertEquals(1L, session.createQuery("select count(u) from User u", Long.class).getSingleResult());
            assertEquals("alex@gmail.com", newUser.getEmail());
            assertEquals(18, newUser.getAge());
        }
    }
}
