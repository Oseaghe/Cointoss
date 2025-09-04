package org.example.cointoss.mappers;

import org.example.cointoss.dtos.RegisterUserRequest;
import org.example.cointoss.dtos.UpdateEmailRequest;
import org.example.cointoss.dtos.UpdateUsernameRequest;
import org.example.cointoss.dtos.UserDto;
import org.example.cointoss.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    @Mapping(target = "name", source = "name")
    User toEntity(RegisterUserRequest request);
    void updateEmail(UpdateEmailRequest request, @MappingTarget User user);
    void updateUsername(UpdateUsernameRequest request, @MappingTarget User user);
}
