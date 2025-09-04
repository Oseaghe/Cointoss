package org.example.cointoss.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class UpdateUsernameRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be less that 255 characters")
        public String name;
}
