package com.JSR.PharmaFlow.DTO;

import jakarta.validation.constraints.Size;
import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


@Data
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

     @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }


}
