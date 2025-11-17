package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.Utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Slf4j
public class Test {

    private final JwtUtil jwtUtil;

    public Test(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    // Add this test endpoint to check JWT functionality
    @GetMapping("/test-jwt")
    public ResponseEntity<?> testJwt(@RequestParam String token) {
        try {
            boolean isValid = jwtUtil.validatePasswordResetToken(token);
            String email = jwtUtil.extractPasswordResetEmail(token);
            boolean isExpired = jwtUtil.isTokenExpired(token);

            return ResponseEntity.ok().body(Map.of(
                    "isValid", isValid,
                    "email", email,
                    "isExpired", isExpired
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/test-hash")
    public ResponseEntity<?> testHash(@RequestParam String token) {
        try {
            String computedHash = jwtUtil.hashToken(token);
            String storedHash = "olijcwk5PhsJOHYMhqS8AO5jV3qPMpgZPTImg6zcSIE=";

            log.info("Token: {}", token);
            log.info("Computed Hash: {}", computedHash);
            log.info("Stored Hash: {}", storedHash);
            log.info("Hashes Match: {}", computedHash.equals(storedHash));

            return ResponseEntity.ok().body(Map.of(
                    "computedHash", computedHash,
                    "storedHash", storedHash,
                    "match", computedHash.equals(storedHash)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
