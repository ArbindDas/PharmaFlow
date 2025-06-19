package com.JSR.PharmaFlow.DTO;


import com.JSR.PharmaFlow.Enums.OAuthProvider;
import com.JSR.PharmaFlow.Enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    private Long id;

    @NotBlank (message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email (message = "Email should be valid")
    private String email;

    @Pattern (regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character")
    private String password;


    @Enumerated (EnumType.STRING)
    @NotNull (message = "Role cannot be null")
    private Set< Role > roles;

    @Enumerated (EnumType.STRING)
    private OAuthProvider authProvider;

}
