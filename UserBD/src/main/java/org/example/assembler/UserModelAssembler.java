package org.example.assembler;

import org.example.controller.UserController;
import org.example.dto.UserResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserResponse, EntityModel<UserResponse>> {

    @Override
    public EntityModel<UserResponse> toModel(UserResponse entity) {
        EntityModel<UserResponse> model = EntityModel.of(
                entity,
                linkTo(methodOn(UserController.class)
                        .findUserById(entity.id())).withSelfRel(),
                linkTo(methodOn(UserController.class)
                        .findAllUsers()).withRel("users")
        );
        return model;
    }
}
