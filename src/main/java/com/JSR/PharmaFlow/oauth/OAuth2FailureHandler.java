package com.JSR.PharmaFlow.oauth;


import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final String frontendOrigin = "http://localhost:5173"; // Consistent with your success handler

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, java.io.IOException {

        String errorMessage = exception.getMessage();
        response.setContentType("text/html");
        response.getWriter().write(
                "<!DOCTYPE html><html><body>" +
                        "<script>" +
                        "if (window.opener) {" +
                        "  window.opener.postMessage(" +
                        "  {" +
                        "    type: 'OAUTH_ERROR'," +
                        "    error: '" + escapeJs(errorMessage) + "'," +
                        "    timestamp: " + System.currentTimeMillis() +
                        "  }, '" + frontendOrigin + "');" +
                        "}" +
                        "window.close();" +
                        "</script>" +
                        "</body></html>"
        );
    }

    // Helper method to escape JavaScript strings
    private String escapeJs(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}