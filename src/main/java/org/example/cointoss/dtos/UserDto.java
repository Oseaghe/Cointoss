package org.example.cointoss.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDto {
    private String name;
    private String email;
    private long id;
}
