package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.User;
import org.example.UserService;
import org.example.assembler.UserModelAssembler;
import org.example.dto.ErrorResponse;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.dto.ValidationErrorResponse;
import org.example.mapper.UserMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Operations with users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserModelAssembler userModelAssembler;

    public UserController(UserService userService, UserMapper userMapper, UserModelAssembler userModelAssembler){
        this.userService = userService;
        this.userMapper = userMapper;
        this.userModelAssembler = userModelAssembler;
    }

    @GetMapping(produces = "application/json")
    @ApiResponse(
        responseCode = "200",
        description = "Users found",
        useReturnTypeSchema = true
    )
    @Operation(summary = "Find all users", description = "Return all users with reference. If users not exists return empty list")
    public CollectionModel<EntityModel<UserResponse>> findAllUsers(){
        List<User> users = userService.findAllUsers();
        List<EntityModel<UserResponse>> userResponses = users.stream()
                                                .map(userMapper::toResponse)
                                                .map(userModelAssembler::toModel)
                                                .toList();
        CollectionModel<EntityModel<UserResponse>> model = CollectionModel.of(
                userResponses,
                linkTo(methodOn(UserController.class)
                        .findAllUsers()).withSelfRel()
        );
        return model;
    }

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(summary = "Find by user ID", description = "Return user with specified ID")
    public ResponseEntity<EntityModel<UserResponse>> findUserById(@PathVariable("id")
                                                     @Parameter(description = "User ID",
                                                             example = "1",
                                                             required = true)
                                                     Long id){
        Optional<User> user = userService.findUser(id);
        if (user.isPresent()){
            return ResponseEntity.ok(userModelAssembler.toModel(userMapper.toResponse(user.get())));
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(produces = "application/json")
    @Operation(summary = "Create User", description = "User must have unique email")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created",
                    headers = @Header(
                            name = "Location",
                            description = "URL of the created user",
                            schema = @Schema(type = "string", example = "/users/5")),
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Data conflict, for example duplicate email",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<EntityModel<UserResponse>> createUser(
            @RequestBody
            @Valid
            UserRequest userRequest
    ){
        User user = userMapper.toEntity(userRequest);
        UserResponse userResponse = userMapper.toResponse(userService.saveUser(user));
        EntityModel<UserResponse> model = userModelAssembler.toModel(userResponse);
        return ResponseEntity.created(URI.create("/users/" + userResponse.id())).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update User", description = "Update name, email and age of user. Email must be unique")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User updated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Data conflict, for example duplicate email",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<Void> updateUser(
            @PathVariable("id")
            @Parameter(description = "User ID",
                    example = "1",
                    required = true)
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
    @Operation(summary = "Delete User", description = "Deleted user with specified ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<Void> deleteUser(
            @PathVariable("id")
            @Parameter(description = "User ID",
                    example = "1",
                    required = true)
            Long id
    ){
        boolean isDeleted = userService.deleteUser(id);

        if(isDeleted){ return ResponseEntity.noContent().build(); }
        else{ return ResponseEntity.notFound().build(); }
    }
}
