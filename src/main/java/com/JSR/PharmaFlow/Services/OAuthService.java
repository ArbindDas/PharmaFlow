package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Enums.OAuthProvider;
import com.JSR.PharmaFlow.Enums.Role;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.AuthProvider;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class OAuthService {

    @Autowired
    private UsersRepository userRepository;

    @Lazy
    @Autowired
    @Qualifier("bcryptPasswordEncoder")  // Add this qualifier
    private PasswordEncoder passwordEncoder;


//    public void processOAuthPostLogin(String email, String name, String picture) {
//        // FIRST: Check if a Google user with this email already exists
//        Optional<Users> optionalGoogleUser = userRepository.findByEmailAndAuthProvider(email, OAuthProvider.GOOGLE);
//
//        if (optionalGoogleUser.isPresent()) {
//            // Google user exists - update their info
//            Users user = optionalGoogleUser.get();
//            if (!user.getFullName().equals(name)) {
//                user.setFullName(name);
//                userRepository.save(user);
//            }
//        } else {
//            // No Google user exists - create a new one
//            Users newUser = new Users();
//            newUser.setEmail(email);
//            newUser.setFullName(name);
//            newUser.setPassword(passwordEncoder.encode("OAUTH_USER_" + UUID.randomUUID().toString()));
//            newUser.setRoles(Set.of(Role.USER));
//            newUser.setAuthProvider(OAuthProvider.GOOGLE);
//            newUser.setCreatedAt(Instant.now());
//            userRepository.save(newUser);
//
//            System.out.println("Created new Google user: " + email);
//        }
//    }


    public void processOAuthPostLogin(String email, String name, String picture) {
        // FIRST: Check if ANY user with this email exists (regular or OAuth)
        Optional<Users> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            Users user = existingUser.get();

            // If it's a regular user, don't convert to Google user
            if (user.getAuthProvider() == null || user.getAuthProvider() == OAuthProvider.LOCAL) {
                // Regular user - just update name if needed
                if (!user.getFullName().equals(name)) {
                    user.setFullName(name);
                    userRepository.save(user);
                }
                return; // Exit after handling regular user
            }

            // Handle Google user update
            if (user.getAuthProvider() == OAuthProvider.GOOGLE) {
                if (!user.getFullName().equals(name)) {
                    user.setFullName(name);
                    userRepository.save(user);
                }
            }
        } else {
            // No user exists - create a new Google user
            Users newUser = new Users();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setPassword(passwordEncoder.encode("OAUTH_USER_" + UUID.randomUUID().toString()));
            newUser.setRoles(Set.of(Role.USER));
            newUser.setAuthProvider(OAuthProvider.GOOGLE);
            newUser.setCreatedAt(Instant.now());
            userRepository.save(newUser);
            System.out.println("Created new Google user: " + email);
        }
    }


}