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
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUser() {
        User user = new User(
                "Alex",
                "alex@gmail.com",
                18);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.findUser(1L);

        assertEquals(Optional.of(user), result);
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound(){
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.findUser(1L);

        assertEquals(Optional.empty(), result);
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldSaveUser(){
        User savedUser =
                new User(
                        "Alex",
                        "alex@gmail.com",
                        18);

        when(userRepository.save(savedUser)).thenReturn(savedUser);
        User result = userService.saveUser(savedUser);

        assertSame(savedUser, result);
        verify(userRepository)
                .save(savedUser);
    }

    @Test
    void shouldDeleteUser(){
        User user =
                new User(
                        "Alex",
                        "alex@gmail.com",
                        18);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldReturnFalseWhenDeletionFails(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        boolean result = userService.deleteUser(1L);

        assertFalse(result);
        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void shouldUpdateUser(){
        User user =
                new User(
                        "Alex",
                        "alex@gmail.com",
                        18);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        boolean result = userService.updateUser(
                1L, "Sasha", "sasha@gmail.com", 23);

        assertTrue(result);
        verify(userRepository).findById(1L);
        assertEquals("Sasha", user.getName());
        assertEquals("sasha@gmail.com", user.getEmail());
        assertEquals(23, user.getAge());
    }

    @Test
    void shouldReturnFalseWhenUpdateFails(){
        Long userID = 1L;
        String name = "Alex";
        String mail = "alex@gmail.com";
        int age = 18;

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        boolean isUpdated = userService.updateUser(userID, name, mail, age);

        assertFalse(isUpdated);
        verify(userRepository).findById(1L);
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

        when(userRepository.findAll()).thenReturn(users);

        assertEquals(users, userService.findAllUsers());
        verify(userRepository)
                .findAll();
    }

    @Test
    void shouldReturnEmptyWhenUsersEmpty(){

        when(userRepository.findAll()).thenReturn(List.of());

        assertEquals(List.of(), userService.findAllUsers());
        verify(userRepository)
                .findAll();
    }
}