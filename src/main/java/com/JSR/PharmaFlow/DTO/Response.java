package com.JSR.PharmaFlow.DTO;

import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;  // Add this import
import java.util.List;
import java.util.stream.Collectors;


public class Response {

    public record JwtResponse(String token, String username, List<String> roles) {
        public JwtResponse(String token, String username, Collection<? extends GrantedAuthority> authorities) {
            this(token, username, authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect( Collectors.toList()));
        }
    }

    public record ApiResponse(boolean success, String message) {
    }
}
