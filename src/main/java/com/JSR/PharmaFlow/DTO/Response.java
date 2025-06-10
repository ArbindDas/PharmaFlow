package com.JSR.PharmaFlow.DTO;

import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;  // Add this import
public class Response {

    public record JwtResponse(String token, String username, Collection<? extends GrantedAuthority> authorities) {
    }

    public record ApiResponse(boolean success, String message) {
    }

}
