package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.DTO.ResetPasswordRequest;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import com.JSR.PharmaFlow.Utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reset")
@CrossOrigin(origins = "http://localhost:5173",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        allowCredentials = "true")
@Slf4j
public class PasswordResetController {

    private final UsersRepository usersRepository;
    private final JwtUtil jwtUtil;


    private final PasswordEncoder passwordEncoder;
    public PasswordResetController(UsersRepository usersRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            log.info("=== TOKEN VALIDATION STARTED ===");
            log.info("Received token: {}", token);

            // Step 1: Validate JWT token structure
            log.info("Step 1: Validating JWT token structure");
            boolean isTokenValid = jwtUtil.validatePasswordResetToken(token);
            log.info("JWT token validation result: {}", isTokenValid);

            if (!isTokenValid) {
                log.error("JWT token validation failed");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid or expired reset token", "type", "error")
                );
            }

            // Step 2: Extract email from token
            log.info("Step 2: Extracting email from token");
            String email = jwtUtil.extractPasswordResetEmail(token);
            log.info("Extracted email: {}", email);

            if (email == null) {
                log.error("Could not extract email from token");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid reset token", "type", "error")
                );
            }

            // Step 3: Find user in database
            log.info("Step 3: Finding user in database");
            Optional<Users> userOptional = usersRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                log.error("User not found for email: {}", email);
                return ResponseEntity.badRequest().body(
                        Map.of("message", "User not found", "type", "error")
                );
            }

            Users user = userOptional.get();
            log.info("User found: {}", user.getEmail());

            // Step 4: Check stored token hash
            log.info("Step 4: Checking stored token hash");
            String storedTokenHash = user.getPasswordResetTokenHash();
            log.info("Stored token hash: {}", storedTokenHash);

            if (storedTokenHash == null) {
                log.error("No token hash stored for user");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid reset token", "type", "error")
                );
            }

            // Step 5: Verify token hash matches
            log.info("Step 5: Verifying token hash matches");
            boolean hashMatches = jwtUtil.verifyTokenHash(token, storedTokenHash);
            log.info("Token hash verification result: {}", hashMatches);

            if (!hashMatches) {
                log.error("Token hash verification failed");
                log.info("Input token: {}", token);
                log.info("Stored hash: {}", storedTokenHash);
                log.info("Computed hash: {}", jwtUtil.hashToken(token));
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid reset token", "type", "error")
                );
            }

            // Step 6: Check token expiry
            log.info("Step 6: Checking token expiry");
            LocalDateTime expiry = user.getPasswordResetTokenExpiry();
            LocalDateTime now = LocalDateTime.now();
            log.info("Token expiry: {}", expiry);
            log.info("Current time: {}", now);
            log.info("Is token expired: {}", expiry.isBefore(now));

            if (expiry.isBefore(now)) {
                log.error("Token has expired");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Reset token has expired", "type", "error")
                );
            }

            log.info("=== TOKEN VALIDATION SUCCESSFUL ===");
            return ResponseEntity.ok().body(
                    Map.of("message", "Token is valid", "type", "success", "token", token)
            );

        } catch (Exception e) {
            log.error("=== TOKEN VALIDATION EXCEPTION ===");
            log.error("Exception during token validation: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Invalid or expired reset token", "type", "error")
            );
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            log.info("=== PASSWORD RESET REQUEST STARTED ===");
            log.info("Request token: {}", request.getToken());
            log.info("New password length: {}", request.getNewPassword().length());

            // Step 1: Validate token
            log.info("Step 1: Validating JWT token");
            boolean isTokenValid = jwtUtil.validatePasswordResetToken(request.getToken());
            log.info("JWT token validation result: {}", isTokenValid);

            if (!isTokenValid) {
                log.error("JWT token validation failed");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid or expired reset token", "type", "error")
                );
            }

            // Step 2: Extract email from token
            log.info("Step 2: Extracting email from token");
            String email = jwtUtil.extractPasswordResetEmail(request.getToken());
            log.info("Extracted email: {}", email);

            if (email == null) {
                log.error("Could not extract email from token");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid token format", "type", "error")
                );
            }

            // Step 3: Find user by email
            log.info("Step 3: Finding user in database");
            Optional<Users> userOptional = usersRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                log.error("User not found for email: {}", email);
                return ResponseEntity.badRequest().body(
                        Map.of("message", "User not found", "type", "error")
                );
            }

            Users user = userOptional.get();
            log.info("User found: {}", user.getEmail());

            // Step 4: Verify token hash matches
            log.info("Step 4: Checking stored token hash");
            String storedTokenHash = user.getPasswordResetTokenHash();
            log.info("Stored token hash: {}", storedTokenHash);

            if (storedTokenHash == null) {
                log.error("No token hash stored for user");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid reset token", "type", "error")
                );
            }

            // Step 5: Verify token hash
            log.info("Step 5: Verifying token hash");
            boolean hashMatches = jwtUtil.verifyTokenHash(request.getToken(), storedTokenHash);
            log.info("Token hash verification result: {}", hashMatches);

            if (!hashMatches) {
                log.error("Token hash verification failed");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid reset token", "type", "error")
                );
            }

            // Step 6: Check token expiry
            log.info("Step 6: Checking token expiry");
            LocalDateTime expiry = user.getPasswordResetTokenExpiry();
            LocalDateTime now = LocalDateTime.now();
            log.info("Token expiry: {}", expiry);
            log.info("Current time: {}", now);
            log.info("Is token expired: {}", expiry.isBefore(now));

            if (expiry.isBefore(now)) {
                log.error("Token has expired");
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Reset token has expired", "type", "error")
                );
            }

            // Step 7: Update password
            log.info("Step 7: Updating password");
            String hashedPassword = passwordEncoder.encode(request.getNewPassword());
            log.info("Password hashed successfully");

            user.setPassword(hashedPassword);
            user.setPasswordResetTokenHash(null);
            user.setPasswordResetTokenExpiry(null);

            usersRepository.save(user);
            log.info("Password updated successfully for user: {}", email);

            log.info("=== PASSWORD RESET SUCCESSFUL ===");
            return ResponseEntity.ok().body(
                    Map.of("message", "Password reset successfully", "type", "success")
            );

        } catch (Exception e) {
            log.error("=== PASSWORD RESET EXCEPTION ===");
            log.error("Exception during password reset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Error resetting password: " + e.getMessage(), "type", "error")
            );
        }
    }

}

