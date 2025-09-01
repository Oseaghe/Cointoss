package org.example.cointoss.mappers;

import org.example.cointoss.dtos.RegisterUserRequest;
import org.example.cointoss.dtos.UpdateUserRequest;
import org.example.cointoss.dtos.UserDto;
import org.example.cointoss.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.context.annotation.Bean;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    @Mapping(target = "name", source = "name")
    User toEntity(RegisterUserRequest request);
    void update(UpdateUserRequest request, @MappingTarget User user);
}
