package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {


    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            // Return user as not authenticated instead of 401
            response.put("authenticated", false);
            response.put("user", null);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", principal.getAttribute("name"));
        userInfo.put("email", principal.getAttribute("email"));
        userInfo.put("picture", principal.getAttribute("picture"));

        response.put("authenticated", true);
        response.put("user", userInfo);

        return ResponseEntity.ok(response);
    }
}