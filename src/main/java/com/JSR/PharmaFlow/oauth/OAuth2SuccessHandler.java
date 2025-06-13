package com.JSR.PharmaFlow.oauth;

import com.JSR.PharmaFlow.Utils.JwtUtil;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;


@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final String frontendOrigin = "http://localhost:5173";

    public OAuth2SuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, java.io.IOException {

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        String state = request.getParameter("state");

        if (state == null || state.isBlank()) {
            sendErrorResponse(response, "Missing state parameter");
            return;
        }

        String token = jwtUtil.generateToken(oauthUser.getEmail());
        response.setContentType("text/html");

        // Using your original code style but with proper escaping
        response.getWriter().write(
                "<!DOCTYPE html><html><body>" +
                        "<script>" +
                        "window.opener.postMessage(" +
                        "{" +
                        "  type: 'OAUTH_SUCCESS'," +
                        "  token: '" + escapeJs(token) + "'," +
                        "  state: '" + escapeJs(state) + "'," +
                        "  timestamp: " + System.currentTimeMillis() +
                        "}, '" + frontendOrigin + "');" +
                        "window.close();" +
                        "</script>" +
                        "</body></html>"
        );
    }

    // Helper method to escape JavaScript strings
    private String escapeJs(String input) {
        return input.replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private void sendErrorResponse(HttpServletResponse response, String error) throws IOException, java.io.IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/html");
        response.getWriter().write(
                "<!DOCTYPE html><html><body>" +
                        "<script>" +
                        "if (window.opener) {" +
                        "  window.opener.postMessage({" +
                        "    type: 'OAUTH_ERROR'," +
                        "    error: '" + escapeJs(error) + "'" +
                        "  }, '" + frontendOrigin + "');" +
                        "}" +
                        "window.close();" +
                        "</script>" +
                        "</body></html>"
        );
    }
}