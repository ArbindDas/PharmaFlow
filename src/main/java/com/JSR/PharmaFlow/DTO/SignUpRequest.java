package com.JSR.PharmaFlow.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class SignUpRequest {

    @NotBlank(message = "fullname must not be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String fullname;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;
    

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&^_-])[A-Za-z\\d@$!%*#?&^_-]{8,}$",
        message = "Password must contain uppercase, lowercase, digit, and special character"
    )
    private String password;
}
