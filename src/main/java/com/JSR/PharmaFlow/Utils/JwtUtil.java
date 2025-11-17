package com.JSR.PharmaFlow.Utils;
import java.nio.charset.StandardCharsets;
import com.JSR.PharmaFlow.Events.JwtToken;
import com.JSR.PharmaFlow.Repository.JwtTokenRepository;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Autowired
    private JwtTokenRepository tokenRepository;

    @Value("${JWT_SECRET}")
    private String secretKey;

    @Value("${JWT_REFRESH_TOKEN_EXPIRATION:3600000}") // default 1 hour
    public long REFRESH_TOKEN_EXPIRATION;

    @Value("${JWT_PASSWORD_RESET_EXPIRATION:3600000}") // 1 hour
    private long PASSWORD_RESET_EXPIRATION;

    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error extracting claims from token: {}", e.getMessage());
            throw e;
        }
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private final long ONE_HOUR = 1000 * 60 * 60; // 1 hour

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ONE_HOUR))
                .signWith(getSigningKey())
                .compact();
    }

//    public String generatePasswordResetToken(String email) {
//        return Jwts.builder()
//                .subject(email)
//                .claim("type", "password_reset")
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + PASSWORD_RESET_EXPIRATION))
//                .signWith(getSigningKey())
//                .compact();
//    }

    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .subject(email)  // ✅ Only email, no timestamp
                .claim("type", "password_reset")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + PASSWORD_RESET_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    // BCrypt token hashing
    public String hashToken(String token) {
        try {
            log.info("Hashing token using BCrypt");
            log.info("Input token length: {}", token.length());

            // Handle BCrypt's 72-byte limit
            byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
            String tokenToHash = token;
            if (tokenBytes.length > 72) {
                tokenToHash = new String(tokenBytes, 0, 72, StandardCharsets.UTF_8);
                log.info("Token truncated to 72 bytes for BCrypt hashing");
            }

            String hash = BCrypt.hashpw(tokenToHash, BCrypt.gensalt());
            log.info("Generated BCrypt hash: {} (length: {})", hash, hash.length());
            return hash;

        } catch (Exception e) {
            log.error("Error hashing token with BCrypt: {}", e.getMessage());
            throw new RuntimeException("Token hashing failed", e);
        }
    }

    // BCrypt token verification
    public boolean verifyTokenHash(String token, String storedTokenHash) {
        try {
            if (storedTokenHash == null || storedTokenHash.trim().isEmpty()) {
                log.error("Stored token hash is null or empty");
                return false;
            }

            log.info("Verifying token hash with BCrypt - Input length: {}, Stored hash: {}...",
                    token.length(), storedTokenHash.substring(0, Math.min(20, storedTokenHash.length())));

            // Handle BCrypt's 72-byte limit
            byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
            String tokenToVerify = token;
            if (tokenBytes.length > 72) {
                tokenToVerify = new String(tokenBytes, 0, 72, StandardCharsets.UTF_8);
            }

            boolean matches = BCrypt.checkpw(tokenToVerify, storedTokenHash);

            log.info("BCrypt hash verification result: {}", matches);
            return matches;

        } catch (Exception e) {
            log.error("Error verifying token hash with BCrypt: {}", e.getMessage());
            return false;
        }
    }

    public boolean validatePasswordResetToken(String token) {
        try {
            log.info("=== JWT PASSWORD RESET TOKEN VALIDATION ===");
            log.info("Token: {}...", token.substring(0, Math.min(50, token.length())));

            if (token == null || token.trim().isEmpty()) {
                log.error("Token is null or empty");
                return false;
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.error("Invalid JWT structure. Expected 3 parts, got: {}", parts.length);
                return false;
            }

            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("type", String.class);
            boolean isPasswordReset = "password_reset".equals(tokenType);
            boolean isExpired = isTokenExpired(token);

            log.info("Token validation - Type: {}, isPasswordReset: {}, isExpired: {}",
                    tokenType, isPasswordReset, isExpired);
            log.info("Token subject: {}", claims.getSubject());
            log.info("Token expiration: {}", claims.getExpiration());

            return isPasswordReset && !isExpired;

        } catch (ExpiredJwtException e) {
            log.error("Password reset token has EXPIRED: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Password reset token is MALFORMED: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.error("Password reset token has INVALID SIGNATURE: {}", e.getMessage());
            log.error("JWT secret key might not match the one used to generate the token");
            return false;
        } catch (JwtException e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage());
            return false;
        }
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    public void storeToken(String token, String username, boolean isRefreshToken) {
        try {
            LocalDateTime expiryDate = extractExpiration(token).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            JwtToken jwtToken = new JwtToken(
                    hashToken(token), // Uses BCrypt
                    username,
                    expiryDate,
                    isRefreshToken
            );
            tokenRepository.save(jwtToken);
        } catch (Exception e) {
            log.error("Error storing token: {}", e.getMessage());
            throw new RuntimeException("Token storage failed", e);
        }
    }

    public boolean isTokenValidAndStored(String token) {
        try {
            String hashedToken = hashToken(token);
            return tokenRepository.existsById(hashedToken) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validatePasswordResetTokenStructure(String token) {
        try {
            log.info("=== JWT PASSWORD RESET TOKEN STRUCTURE VALIDATION ===");
            log.info("Token: {}...", token.substring(0, Math.min(50, token.length())));

            if (token == null || token.trim().isEmpty()) {
                log.error("Token is null or empty");
                return false;
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.error("Invalid JWT structure. Expected 3 parts, got: {}", parts.length);
                return false;
            }

            Claims claims = extractAllClaimsIgnoreExpiry(token);
            String tokenType = claims.get("type", String.class);
            boolean isPasswordReset = "password_reset".equals(tokenType);

            log.info("Token structural validation - Type: {}, isPasswordReset: {}",
                    tokenType, isPasswordReset);
            log.info("Token subject: {}", claims.getSubject());
            log.info("Token expiration: {}", claims.getExpiration());

            return isPasswordReset;

        } catch (MalformedJwtException e) {
            log.error("Password reset token is MALFORMED: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.error("Password reset token has INVALID SIGNATURE: {}", e.getMessage());
            log.error("JWT secret key might not match the one used to generate the token");
            return false;
        } catch (JwtException e) {
            log.error("JWT structural validation error: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during token structural validation: {}", e.getMessage());
            return false;
        }
    }

    // Extract claims without checking expiry
    private Claims extractAllClaimsIgnoreExpiry(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // Return the claims even if expired - we'll check database expiry instead
            log.info("Token is expired but returning claims for structural validation");
            return e.getClaims();
        } catch (Exception e) {
            log.error("Error extracting claims from token: {}", e.getMessage());
            throw e;
        }
    }


    public String extractPasswordResetEmail(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String subject = claims.getSubject();

            if (subject != null && subject.contains(":")) {
                // Extract just the email part (before the first colon)
                String email = subject.substring(0, subject.indexOf(':'));
                log.info("Extracted email from token: {}", email);
                return email;
            }

            log.info("Using full subject as email: {}", subject);
            return subject;
        } catch (Exception e) {
            log.error("Error extracting email from token: {}", e.getMessage());
            return null;
        }
    }
}