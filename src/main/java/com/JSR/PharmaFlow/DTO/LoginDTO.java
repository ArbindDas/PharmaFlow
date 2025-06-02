package com.JSR.PharmaFlow.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class LoginDTO {


    @NotBlank (message = "Email cannot be empty")
    @Email (message = "Email must be a valid email address")
    private String email;


    @NotBlank (message = "Password cannot be empty")
    @Pattern (
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$",
            message = "Password must be at least 8 characters long, include at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    private String password;
}
