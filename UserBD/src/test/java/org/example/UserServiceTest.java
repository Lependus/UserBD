package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDaoImpl userDao;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUser() {
        User user = new User(
                "Alex",
                "alex@gmail.com",
                18);

        when(userDao.findById(1L))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.findUser(1L);

        assertEquals(Optional.of(user), result);
        verify(userDao).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound(){
        when(userDao.findById(1L))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.findUser(1L);

        assertEquals(Optional.empty(), result);
        verify(userDao).findById(1L);
    }

    @Test
    void shouldSaveUser(){
        User savedUser =
                new User(
                        "Alex",
                        "alex@gmail.com",
                        18);

        userService.saveUser(savedUser);

        verify(userDao)
                .save(savedUser);
    }

    @Test
    void shouldDeleteUser(){
        Long userID = 1L;

        when(userDao.delete(userID)).thenReturn(true);
        boolean isDeleted = userService.deleteUser(userID);

        assertTrue(isDeleted);
        verify(userDao)
                .delete(userID);
    }

    @Test
    void shouldReturnFalseWhenDeletionFails(){
        Long userID = 1L;

        when(userDao.delete(userID)).thenReturn(false);
        boolean isDeleted = userService.deleteUser(userID);

        assertFalse(isDeleted);
        verify(userDao)
                .delete(userID);
    }

    @Test
    void shouldUpdateUser(){
        Long userID = 1L;
        String name = "Alex";
        String mail = "alex@gmail.com";
        int age = 18;

        when(userDao.update(userID, name, mail, age)).thenReturn(true);
        boolean isUpdated = userService.updateUser(userID, name, mail, age);

        assertTrue(isUpdated);
        verify(userDao)
                .update(userID, name, mail, age);
    }

    @Test
    void shouldReturnFalseWhenUpdateFails(){
        Long userID = 1L;
        String name = "Alex";
        String mail = "alex@gmail.com";
        int age = 18;

        when(userDao.update(userID, name, mail, age)).thenReturn(false);
        boolean isUpdated = userService.updateUser(userID, name, mail, age);

        assertFalse(isUpdated);
        verify(userDao)
                .update(userID, name, mail, age);
    }

    @Test
    void shouldReturnAllUsers(){
        List<User> users = new ArrayList<>();
        users.add(new User(
                "Alex",
                "alex@gmail.com",
                18));
        users.add(new User(
                "Sasha",
                "sasha@gmail.com",
                23));

        when(userDao.findAll()).thenReturn(users);

        assertEquals(users, userService.findAllUsers());
        verify(userDao)
                .findAll();
    }

    @Test
    void shouldReturnEmptyWhenUsersEmpty(){

        when(userDao.findAll()).thenReturn(List.of());

        assertEquals(List.of(), userService.findAllUsers());
        verify(userDao)
                .findAll();
    }
}