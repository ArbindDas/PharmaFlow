package com.JSR.PharmaFlow.DTO;


import com.JSR.PharmaFlow.Enums.Role;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UserProfileDto {


    @NotBlank (message = "Email cannot be empty")
    @Email (message = "Email must be a valid email address")
    @Column (name = "email", nullable = false , unique = true)
    private String email;

    @Column (name = "role")
    @NotNull (message = "Role cannot be null")
    private Set < Role > roles;
}
