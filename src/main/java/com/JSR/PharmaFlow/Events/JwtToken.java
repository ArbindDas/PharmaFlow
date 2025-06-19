package com.JSR.PharmaFlow.Events;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_tokens")
@Data
public class JwtToken {

    @Id
    private String token;  // The actual JWT string or its hash

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean isRefreshToken;

    // Constructors
    public JwtToken() {}

    public JwtToken(String token, String username, LocalDateTime expiryDate, boolean isRefreshToken) {
        this.token = token;
        this.username = username;
        this.expiryDate = expiryDate;
        this.isRefreshToken = isRefreshToken;
    }

    // Getters and Setters
    // ...
}