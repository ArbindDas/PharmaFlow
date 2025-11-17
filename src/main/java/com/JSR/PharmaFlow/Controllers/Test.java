//package com.JSR.PharmaFlow.Controllers;
//
//import com.JSR.PharmaFlow.Utils.JwtUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//
//
//@Slf4j
//@RestController
//@RequestMapping("/api/test")
//public class Test {
//
//    private final JwtUtil jwtUtil;
//
//    public Test(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    @GetMapping("/test-jwt")
//    public ResponseEntity<?> testJwt(@RequestParam String token) {
//        try {
//            boolean isValid = jwtUtil.validatePasswordResetToken(token);
//            String email = jwtUtil.extractPasswordResetEmail(token);
//            boolean isExpired = jwtUtil.isTokenExpired(token);
//
//            return ResponseEntity.ok().body(Map.of(
//                    "isValid", isValid,
//                    "email", email,
//                    "isExpired", isExpired
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    @GetMapping("/test-hash")
//    public ResponseEntity<?> testHash(@RequestParam String token) {
//        try {
//            log.info("=== COMPREHENSIVE HASH TEST ===");
//            log.info("Token: {}...", token.substring(0, Math.min(50, token.length())));
//
//            // Test 1: BCrypt hashing
//            String computedBcryptHash = jwtUtil.hashToken(token);
//
//            // Your stored SHA-256 hash
//            String storedSha256Hash = "olijcwk5PhsJOHYMhqS8AO5jV3qPMpgZPTImg6zcSIE=";
//
//            // Test 2: Try to migrate SHA-256 to BCrypt
//            String migratedBcryptHash = jwtUtil.migrateSha256ToBcrypt(token, storedSha256Hash);
//
//            // Test 3: Direct BCrypt verification (should fail with SHA-256 hash)
//            boolean directBcryptMatch = jwtUtil.verifyTokenHash(token, storedSha256Hash);
//
//            // Test 4: Verify with migrated hash (if migration successful)
//            boolean migratedBcryptMatch = migratedBcryptHash != null &&
//                    jwtUtil.verifyTokenHash(token, migratedBcryptHash);
//
//            log.info("Computed BCrypt Hash: {}", computedBcryptHash);
//            log.info("Stored SHA-256 Hash: {}", storedSha256Hash);
//            log.info("Migrated BCrypt Hash: {}", migratedBcryptHash);
//            log.info("Direct BCrypt Match: {}", directBcryptMatch);
//            log.info("Migrated BCrypt Match: {}", migratedBcryptMatch);
//
//            return ResponseEntity.ok().body(Map.of(
//                    "computedBcryptHash", computedBcryptHash,
//                    "storedSha256Hash", storedSha256Hash,
//                    "migratedBcryptHash", migratedBcryptHash != null ? migratedBcryptHash : "Migration failed",
//                    "migrationSuccessful", migratedBcryptHash != null,
//                    "directBcryptMatch", directBcryptMatch,
//                    "migratedBcryptMatch", migratedBcryptMatch,
//                    "algorithm", "BCrypt with SHA-256 migration"
//            ));
//
//        } catch (Exception e) {
//            log.error("Test hash error: {}", e.getMessage(), e);
//            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    // NEW: Generate a fresh token for testing
//    @GetMapping("/generate-test-token")
//    public ResponseEntity<?> generateTestToken(@RequestParam String email) {
//        try {
//            String newToken = jwtUtil.generatePasswordResetToken(email);
//            String hash = jwtUtil.hashToken(newToken);
//
//            log.info("Generated new token for: {}", email);
//            log.info("Token: {}", newToken);
//            log.info("Hash: {}", hash);
//
//            // Test the token immediately
//            boolean isValid = jwtUtil.validatePasswordResetToken(newToken);
//            String extractedEmail = jwtUtil.extractPasswordResetEmail(newToken);
//
//            return ResponseEntity.ok().body(Map.of(
//                    "token", newToken,
//                    "hash", hash,
//                    "isValid", isValid,
//                    "extractedEmail", extractedEmail,
//                    "message", "Use this token for testing"
//            ));
//
//        } catch (Exception e) {
//            log.error("Generate token error: {}", e.getMessage(), e);
//            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
//        }
//    }
//}

package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.Utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class Test {

    private final JwtUtil jwtUtil;



    public Test(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/test-jwt")
    public ResponseEntity<?> testJwt(@RequestParam String token) {
        try {
            boolean isValid = jwtUtil.validatePasswordResetToken(token);
            String email = jwtUtil.extractPasswordResetEmail(token);
            boolean isExpired = jwtUtil.isTokenExpired(token);

            // Decode token for analysis
            String[] tokenParts = token.split("\\.");
            String header = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]));

            return ResponseEntity.ok().body(Map.of(
                    "isValid", isValid,
                    "email", email,
                    "isExpired", isExpired,
                    "tokenHeader", header,
                    "tokenPayload", payload
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test-hash")
    public ResponseEntity<?> testHash(@RequestParam String token) {
        try {
            log.info("=== COMPREHENSIVE HASH ANALYSIS ===");
            log.info("Token: {}...", token.substring(0, Math.min(50, token.length())));

            // Analyze the token first
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid JWT token structure"));
            }

            // Test 1: Generate new BCrypt hash
            String computedBcryptHash = jwtUtil.hashToken(token);

            // Test 2: Your stored SHA-256 hash (base64 encoded)
            String storedSha256Hash = "olijcwk5PhsJOHYMhqS8AO5jV3qPMpgZPTImg6zcSIE=";

            // Test 3: Verify if we can detect the algorithm
            String detectedAlgorithm = detectHashAlgorithm(storedSha256Hash);

            // Test 4: Create SHA-256 hash for comparison
            String computedSha256Hash = computeSha256Hash(token);

            // Test 5: Compare SHA-256 hashes
            boolean sha256Match = computedSha256Hash.equals(storedSha256Hash);

            // Test 6: BCrypt verification (should work for new tokens)
            boolean bcryptValid = jwtUtil.verifyTokenHash(token, computedBcryptHash);

            log.info("Token Analysis:");
            log.info("- Parts: {} segments", tokenParts.length);
            log.info("- Computed BCrypt: {}", computedBcryptHash);
            log.info("- Stored Hash: {}", storedSha256Hash);
            log.info("- Detected Algorithm: {}", detectedAlgorithm);
            log.info("- Computed SHA-256: {}", computedSha256Hash);
            log.info("- SHA-256 Match: {}", sha256Match);
            log.info("- BCrypt Valid: {}", bcryptValid);

            return ResponseEntity.ok().body(Map.of(
                    "tokenParts", tokenParts.length,
                    "computedBcryptHash", computedBcryptHash,
                    "storedHash", storedSha256Hash,
                    "detectedAlgorithm", detectedAlgorithm,
                    "computedSha256Hash", computedSha256Hash,
                    "sha256Match", sha256Match,
                    "bcryptValid", bcryptValid,
                    "tokenLength", token.length()
            ));

        } catch (Exception e) {
            log.error("Hash test error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/analyze-token")
    public ResponseEntity<?> analyzeToken(@RequestParam String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid JWT format"));
            }

            String header = new String(Base64.getUrlDecoder().decode(parts[0]));
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            String signature = parts[2];

            return ResponseEntity.ok().body(Map.of(
                    "header", header,
                    "payload", payload,
                    "signatureLength", signature.length(),
                    "tokenLength", token.length(),
                    "isValidStructure", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token: " + e.getMessage()));
        }
    }

    @GetMapping("/generate-test-token")
    public ResponseEntity<?> generateTestToken(@RequestParam String email) {
        try {
            String newToken = jwtUtil.generatePasswordResetToken(email);
            String bcryptHash = jwtUtil.hashToken(newToken);
            String sha256Hash = computeSha256Hash(newToken);

            // Test validation
            boolean isValid = jwtUtil.validatePasswordResetToken(newToken);
            String extractedEmail = jwtUtil.extractPasswordResetEmail(newToken);

            return ResponseEntity.ok().body(Map.of(
                    "token", newToken,
                    "bcryptHash", bcryptHash,
                    "sha256Hash", sha256Hash,
                    "isValid", isValid,
                    "extractedEmail", extractedEmail,
                    "tokenAnalysis", analyzeToken(newToken).getBody()
            ));

        } catch (Exception e) {
            log.error("Generate token error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Utility methods
    private String detectHashAlgorithm(String hash) {
        if (hash == null) return "unknown";

        // BCrypt hashes start with $2a$, $2b$, etc.
        if (hash.startsWith("$2a$") || hash.startsWith("$2b$")) {
            return "BCrypt";
        }

        // Base64 encoded SHA-256 is 44 characters (32 bytes encoded)
        if (hash.length() == 44 && hash.matches("^[A-Za-z0-9+/]+={0,2}$")) {
            return "SHA-256 (Base64)";
        }

        // Hex encoded SHA-256 is 64 characters
        if (hash.length() == 64 && hash.matches("^[0-9a-fA-F]+$")) {
            return "SHA-256 (Hex)";
        }

        return "unknown";
    }

    private String computeSha256Hash(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 computation failed", e);
        }
    }
}