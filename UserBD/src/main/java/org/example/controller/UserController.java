package org.example.controller;

import jakarta.validation.Valid;
import org.example.User;
import org.example.UserService;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper){
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserResponse> findAllUsers(){
        List<User> users = userService.findAllUsers();
        List<UserResponse> userResponses = users.stream()
                                                .map(userMapper::toResponse)
                                                .toList();
        return userResponses;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findUserById(@PathVariable("id") Long id){
        Optional<User> user = userService.findUser(id);
        if (user.isPresent()){
            return ResponseEntity.ok(userMapper.toResponse(user.get()));
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody
            @Valid
            UserRequest userRequest
    ){
        User user = userMapper.toEntity(userRequest);
        UserResponse userResponse = userMapper.toResponse(userService.saveUser(user));
        return ResponseEntity.created(URI.create("/users/" + userResponse.id())).body(userResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable("id")
            Long id,
            @RequestBody
            @Valid
            UserRequest userRequest
    ){
        boolean isUpdated = userService.updateUser(
                    id,
                    userRequest.name(),
                    userRequest.email(),
                    userRequest.age()
        );

        if(isUpdated){ return ResponseEntity.noContent().build(); }
        else{ return ResponseEntity.notFound().build(); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("id")
            Long id
    ){
        boolean isDeleted = userService.deleteUser(id);

        if(isDeleted){ return ResponseEntity.noContent().build(); }
        else{ return ResponseEntity.notFound().build(); }
    }
}
