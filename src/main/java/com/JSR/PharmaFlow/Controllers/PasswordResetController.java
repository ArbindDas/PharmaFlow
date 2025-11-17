package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.DTO.ResetPasswordRequest;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import com.JSR.PharmaFlow.Utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    private static final String INVALID_TOKEN_MESSAGE = "Invalid or expired reset token";
    private static final String SUCCESS_MESSAGE = "Password reset successfully";

    private final UsersRepository usersRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UsersRepository usersRepository,
                                   JwtUtil jwtUtil,
                                   PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ ADD THIS ENDPOINT - For generating and storing reset tokens
    @PostMapping("/generate-reset-token")
    public ResponseEntity<Map<String, String>> generateResetToken(@RequestParam String email) {
        try {
            log.info("Generating reset token for: {}", email);

            // Find user
            Optional<Users> userOptional = usersRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return buildErrorResponse("User not found");
            }

            Users user = userOptional.get();

            // Generate token and hash
            String token = jwtUtil.generatePasswordResetToken(email);
            String tokenHash = jwtUtil.hashToken(token);

            // Store in database
            user.setPasswordResetTokenHash(tokenHash);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            usersRepository.save(user);

            log.info("Reset token generated and stored for: {}", email);

            return ResponseEntity.ok().body(Map.of(
                    "token", token,
                    "message", "Reset token generated successfully",
                    "type", "success"
            ));

        } catch (Exception e) {
            log.error("Error generating reset token: {}", e.getMessage());
            return buildErrorResponse("Error generating reset token");
        }
    }

    @GetMapping("/reset-password")
    public ResponseEntity<Map<String, String>> validateResetToken(@RequestParam String token) {
        log.info("Validating reset token, length: {}", token.length());

        try {
            // Step 1: Validate JWT token structure and signature
            if (!jwtUtil.validatePasswordResetTokenStructure(token)) {
                log.warn("Invalid JWT token structure");
                return buildErrorResponse(INVALID_TOKEN_MESSAGE);
            }

            // Step 2: Extract email from token
            String email = jwtUtil.extractPasswordResetEmail(token);
            if (email == null || email.trim().isEmpty()) {
                log.warn("Could not extract email from token");
                return buildErrorResponse(INVALID_TOKEN_MESSAGE);
            }

            // Step 3: Find user by email
            Optional<Users> userOptional = usersRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                log.warn("User not found for email: {}", email);
                return buildErrorResponse("User not found");
            }

            Users user = userOptional.get();

            // Step 4: Validate stored token hash using BCrypt
            if (!validateStoredTokenHash(token, user)) {
                return buildErrorResponse(INVALID_TOKEN_MESSAGE);
            }

            // Step 5: Check token expiry from database
            if (isTokenExpired(user)) {
                return buildErrorResponse("Reset token has expired");
            }

            log.info("Token validation successful for user: {}", email);
            return buildSuccessResponse("Token is valid", email);

        } catch (Exception e) {
            log.error("Token validation exception: {}", e.getMessage());
            return buildErrorResponse(INVALID_TOKEN_MESSAGE);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Processing password reset request");

        try {
            // Validate token first
            ResponseEntity<Map<String, String>> validationResponse = validateResetToken(request.getToken());
            if (validationResponse.getStatusCode() != HttpStatus.OK) {
                return validationResponse;
            }

            // Extract email and find user
            String email = jwtUtil.extractPasswordResetEmail(request.getToken());
            Users user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update password and clear reset token
            updateUserPassword(user, request.getNewPassword());

            log.info("Password reset successful for user: {}", email);
            return buildSuccessResponse(SUCCESS_MESSAGE);

        } catch (Exception e) {
            log.error("Password reset exception: {}", e.getMessage());
            return buildErrorResponse("Error resetting password");
        }
    }

    /**
     * Validates the stored token hash against the provided token using BCrypt
     */
    private boolean validateStoredTokenHash(String token, Users user) {
        String storedTokenHash = user.getPasswordResetTokenHash();

        if (storedTokenHash == null || storedTokenHash.trim().isEmpty()) {
            log.warn("No token hash stored for user: {}", user.getEmail());
            return false;
        }

        log.debug("Verifying token hash using BCrypt for user: {}", user.getEmail());
        boolean isValid = jwtUtil.verifyTokenHash(token, storedTokenHash);

        if (!isValid) {
            log.warn("Token hash verification failed for user: {}", user.getEmail());
        }

        return isValid;
    }

    /**
     * Checks if the token has expired based on database expiry timestamp
     */
    private boolean isTokenExpired(Users user) {
        LocalDateTime expiry = user.getPasswordResetTokenExpiry();

        if (expiry == null) {
            log.warn("No expiry date stored for user: {}", user.getEmail());
            return true;
        }

        boolean isExpired = expiry.isBefore(LocalDateTime.now());
        if (isExpired) {
            log.info("Token expired for user: {}, expiry: {}", user.getEmail(), expiry);
        }

        return isExpired;
    }

    /**
     * Updates user password and clears reset token fields
     */
    private void updateUserPassword(Users user, String newPassword) {
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetTokenExpiry(null);

        usersRepository.save(user);
        log.debug("Password updated and reset token cleared for user: {}", user.getEmail());
    }

    /**
     * Builds a standardized error response
     */
    private ResponseEntity<Map<String, String>> buildErrorResponse(String message) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "message", message,
                        "type", "error"
                ));
    }

    /**
     * Builds a standardized success response
     */
    private ResponseEntity<Map<String, String>> buildSuccessResponse(String message) {
        return ResponseEntity.ok()
                .body(Map.of(
                        "message", message,
                        "type", "success"
                ));
    }

    /**
     * Builds a success response with email
     */
    private ResponseEntity<Map<String, String>> buildSuccessResponse(String message, String email) {
        return ResponseEntity.ok()
                .body(Map.of(
                        "message", message,
                        "type", "success",
                        "email", email
                ));
    }
}