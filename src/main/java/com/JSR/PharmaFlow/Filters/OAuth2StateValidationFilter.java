package com.JSR.PharmaFlow.Filters;

import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class OAuth2StateValidationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuth2StateValidationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("/login/oauth2/code/google".equals(request.getRequestURI())) {
            String requestState = request.getParameter("state");
            String cookieState = getStateFromCookie(request); // Or header

            log.info("Validating state - Expected: {}, Received: {}", cookieState, requestState);

            if (requestState == null || !requestState.equals(cookieState)) {
                response.sendError( HttpStatus.UNAUTHORIZED.value(), "Invalid state parameter");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getStateFromCookie(HttpServletRequest request) {
        // Extract state from cookie or header (e.g., "X-OAuth-State")
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("OAUTH2_STATE".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}