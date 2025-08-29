package org.example.cointoss.mappers;

import org.example.cointoss.dtos.RegisterUserRequest;
import org.example.cointoss.dtos.UpdateUserRequest;
import org.example.cointoss.dtos.UserDto;
import org.example.cointoss.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(RegisterUserRequest request);
    void update(UpdateUserRequest request, @MappingTarget User user);
}
