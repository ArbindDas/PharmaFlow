package com.JSR.PharmaFlow.Utils;


import com.JSR.PharmaFlow.Events.JwtToken;
import com.JSR.PharmaFlow.Repository.JwtTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Autowired
    private JwtTokenRepository tokenRepository;


    @Value ("${jwt.secret-key}")
    private String secretKey;

    //
//    public static final long REFRESH_TOKEN_EXPIRATION = 60 * 60 * 1000; // 1 hour in milliseconds
    @Value ("${jwt.refresh-token-expiration}")
    public static long REFRESH_TOKEN_EXPIRATION;


    public SecretKey getSigningKey( ) {
        return Keys.hmacShaKeyFor( secretKey.getBytes() );
    }

    public String extractUsername( String token ) {
        Claims claims = extractAllClaims( token );
        return claims.getSubject();
    }

    public Date extractExpiration( String token ) {
        return extractAllClaims( token ).getExpiration();
    }

    private Claims extractAllClaims( String token ) {
        return Jwts.parser()
                .verifyWith( getSigningKey() )
                .build()
                .parseSignedClaims( token )
                .getPayload();
    }


    public String generateRefreshToken( String username ) {
        return Jwts.builder().subject( username ).issuedAt( new Date() ).expiration( new Date( System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION ) )
                .signWith( getSigningKey() ) // just the key, no algorithm here
                .compact();
    }


    private Boolean isTokenExpired( String token ) {
        return extractExpiration( token ).before( new Date() );
    }

    public String generateToken( String username ) {
        Map< String, Object > claims = new HashMap<>();
        return createToken( claims, username );
    }



    long FIFTEEN_MINUTES = 1000 * 60 * 15; // -> 15 minutes
    long ONE_HOUR = 1000 * 60 * 60; // -> 1 hours
    long ONE_DAY = 1000 * 60 * 60 * 24; // -> one day

    private String createToken( Map< String, Object > claims, String subject ) {
        return Jwts.builder()
                .claims( claims )
                .subject( subject )
                .header().empty().add( "typ", "JWT" )
                .and()
                .issuedAt( new Date( System.currentTimeMillis() ) )
//                .expiration( new Date( System.currentTimeMillis() + 1000 * 60 * 60 ) ) // 1 hour expiration
                .expiration(new Date(System.currentTimeMillis()+ONE_HOUR))
                .signWith( getSigningKey() )
                .compact();
    }

    // Add these methods to your JWT utility class
    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("type", "password_reset")  // Special claim to identify reset tokens
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validatePasswordResetToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "password_reset".equals(claims.get("type")) &&
                    !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

//    public String hashToken( String token ) {
//        // Implement proper token hashing (e.g., using BCrypt)
//        return BCrypt.hashpw(token, BCrypt.gensalt());
//    }

    public String hashToken(String token) {
        // Ensure token is not longer than 72 bytes for BCrypt
        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        if (tokenBytes.length > 72) {
            token = new String(tokenBytes, 0, 72, StandardCharsets.UTF_8);
        }
        return BCrypt.hashpw(token, BCrypt.gensalt());
    }


    public Boolean validateToken( String token ) {
        return ! isTokenExpired( token );
    }



    public void storeToken(String token, String username, boolean isRefreshToken) {
        LocalDateTime expiryDate = extractExpiration(token).toInstant()
                .atZone( ZoneId.systemDefault())
                .toLocalDateTime();

        JwtToken jwtToken = new JwtToken(
                hashToken(token), // Store hashed token for security
                username,
                expiryDate,
                isRefreshToken
        );
        tokenRepository.save(jwtToken);
    }

    public boolean isTokenValidAndStored(String token) {
        try {
            String hashedToken = hashToken(token);
            return tokenRepository.existsById(hashedToken) &&
                    !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }


}


