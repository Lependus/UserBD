package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.catalina.mapper.Mapper;
import org.example.User;
import org.example.UserService;
import org.example.assembler.UserModelAssembler;
import org.example.dto.UserRequest;
import org.example.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({UserMapper.class, UserModelAssembler.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @Test
    void shouldFindUser() throws Exception {
        User user = new User("AAAA", "aaaa@gmail.com", 20);
        ReflectionTestUtils.setField(user, "id", 2L);
        user.setCreatedAt(Instant.parse("2026-10-09T02:00:00Z"));

        when(userService.findUser(2L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("AAAA"))
                .andExpect(jsonPath("$.email").value("aaaa@gmail.com"))
                .andExpect(jsonPath("$.age").value(20))
                .andExpect(jsonPath("$.createdAt").value("2026-10-09T02:00:00Z"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/users/2"))
                .andExpect(jsonPath("$._links.users.href").value("http://localhost/users"));

        verify(userService).findUser(2L);
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.findUser(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        verify(userService).findUser(99L);
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        User user1 = new User(
                "Alex",
                "alex@gmail.com",
                18);
        User user2 = new User(
                "Sasha",
                "sasha@gmail.com",
                23);
        ReflectionTestUtils.setField(user1, "id", 1L);
        ReflectionTestUtils.setField(user2, "id", 2L);
        user1.setCreatedAt(Instant.parse("2026-10-09T02:00:00Z"));
        user2.setCreatedAt(Instant.parse("2026-10-09T02:00:00Z"));
        List<User> users = List.of(user1, user2);

        when(userService.findAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userResponseList.length()").value(2))
                .andExpect(jsonPath("$._embedded.userResponseList[0].name").value("Alex"))
                .andExpect(jsonPath("$._embedded.userResponseList[0].email").value("alex@gmail.com"))
                .andExpect(jsonPath("$._embedded.userResponseList[0].age").value(18))
                .andExpect(jsonPath("$._embedded.userResponseList[0].createdAt").value("2026-10-09T02:00:00Z"))
                .andExpect(jsonPath("$._embedded.userResponseList[1].name").value("Sasha"))
                .andExpect(jsonPath("$._embedded.userResponseList[1].age").value(23))
                .andExpect(jsonPath("$._embedded.userResponseList[1].email").value("sasha@gmail.com"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/users"))
                .andExpect(jsonPath("$._embedded.userResponseList[0]._links.self.href").value("http://localhost/users/1"))
                .andExpect(jsonPath("$._embedded.userResponseList[1]._links.self.href").value("http://localhost/users/2"))
                .andExpect(jsonPath("$._embedded.userResponseList[0]._links.users.href").value("http://localhost/users"))
                .andExpect(jsonPath("$._embedded.userResponseList[1]._links.users.href").value("http://localhost/users"));

        verify(userService).findAllUsers();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/users"))
                .andExpect(jsonPath("$._embedded").doesNotExist());

        verify(userService).findAllUsers();
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Alex",
                "alex@gmail.com",
                23);
        User user = new User(
                "Alex",
                "alex@gmail.com",
                23);
        user.setCreatedAt(Instant.parse("2026-10-09T02:00:00Z"));
        ReflectionTestUtils.setField(user, "id", 5L);
        when(userService.saveUser(any(User.class))).thenReturn(user);

        String json = objectMapper.writeValueAsString(userRequest);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        mockMvc.perform(post("/users").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Alex"))
                .andExpect(jsonPath("$.email").value("alex@gmail.com"))
                .andExpect(jsonPath("$.age").value(23))
                .andExpect(jsonPath("$.createdAt").value("2026-10-09T02:00:00Z"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/users/5"))
                .andExpect(jsonPath("$._links.users.href").value("http://localhost/users"));


        verify(userService).saveUser(captor.capture());
        assertEquals("Alex", captor.getValue().getName());
        assertEquals("alex@gmail.com", captor.getValue().getEmail());
        assertEquals(23, captor.getValue().getAge());
    }

    @Test
    void shouldReturn400WhenCreateUserRequestInvalid () throws Exception {
        UserRequest userRequest = new UserRequest(
                "",
                "alex@gmail.com",
                -1);
        String json = objectMapper.writeValueAsString(userRequest);


        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка проверки данных"))
                .andExpect(jsonPath("$.errors.name").value("Имя не должно быть пустым."))
                .andExpect(jsonPath("$.errors.age").value("Возраст не должен быть отрицательным."));

        verifyNoInteractions(userService);
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Alex",
                "alex@gmail.com",
                23);
        String json = objectMapper.writeValueAsString(userRequest);
        when(userService.updateUser(2L, "Alex", "alex@gmail.com", 23)).thenReturn(true);

        mockMvc.perform(put("/users/2").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(userService).updateUser(2L, "Alex", "alex@gmail.com", 23);
    }

    @Test
    void shouldReturn404WhenUpdatingUserNotFound() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Alex",
                "alex@gmail.com",
                23);
        String json = objectMapper.writeValueAsString(userRequest);
        when(userService.updateUser(99L, "Alex", "alex@gmail.com", 23)).thenReturn(false);

        mockMvc.perform(put("/users/99").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
        verify(userService).updateUser(99L, "Alex", "alex@gmail.com", 23);
    }

    @Test
    void shouldReturn400WhenUpdateUserRequestInvalid() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Alex",
                "alex@localhost",
                23);
        String json = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(put("/users/2").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка проверки данных"))
                .andExpect(jsonPath("$.errors.email").value("Некорректный Email."));

        verifyNoInteractions(userService);
    }

    @Test
    void shouldDeleteUser() throws Exception {
        when(userService.deleteUser(2L)).thenReturn(true);

        mockMvc.perform(delete("/users/2"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).deleteUser(2L);
    }

    @Test
    void shouldReturn404WhenDeletingUserNotFound() throws Exception {
        when(userService.deleteUser(99L)).thenReturn(false);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        verify(userService).deleteUser(99L);
    }

    @Test
    void shouldReturn409WhenCreatingUserWithDuplicateEmail() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Alex",
                "alex@gmail.com",
                23);
        String json = objectMapper.writeValueAsString(userRequest);

        when(userService.saveUser(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate Email"));

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Данные нарушают ограничения базы данных. Проверьте уникальность email"));

        verify(userService).saveUser(any(User.class));
    }

    @Test
    void shouldReturn409WhenUpdatingUserWithDuplicateEmail() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Alex",
                "alex@gmail.com",
                23);
        String json = objectMapper.writeValueAsString(userRequest);

        when(userService.updateUser(2L,
                "Alex",
                "alex@gmail.com",
                23)).thenThrow(new DataIntegrityViolationException("Duplicate Email"));

        mockMvc.perform(put("/users/2").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Данные нарушают ограничения базы данных. Проверьте уникальность email"));

        verify(userService).updateUser(2L,
                "Alex",
                "alex@gmail.com",
                23);
    }
}