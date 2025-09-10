package com.JSR.PharmaFlow.oauth;

import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Filters.JwtFilter;
import com.JSR.PharmaFlow.Services.CustomUserDetailsService;
import com.JSR.PharmaFlow.Services.OAuthService;
import com.JSR.PharmaFlow.Services.UsersService;
import com.JSR.PharmaFlow.Services.UsersService;
import com.JSR.PharmaFlow.Utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {


    private final JwtUtil jwtUtil;
    private final OAuthService oauthService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public OAuth2SuccessHandler(JwtUtil jwtUtil,
                                OAuthService oauthService,  // Use the new service
                                CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.oauthService = oauthService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user information
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // Process the OAuth login
        oauthService.processOAuthPostLogin(email, name, picture);

        // Load user details properly
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Get the actual username/email from UserDetails
        String username = userDetails.getUsername();

        // Generate JWT tokens with proper username
        String accessToken = jwtUtil.generateToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        // URL encode the tokens to handle special characters
        String encodedAccessToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        String encodedRefreshToken = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        // Redirect to frontend with tokens
        String redirectUrl = "http://localhost:5173/oauth-success?accessToken=" + encodedAccessToken +
                "&refreshToken=" + encodedRefreshToken;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }


}

