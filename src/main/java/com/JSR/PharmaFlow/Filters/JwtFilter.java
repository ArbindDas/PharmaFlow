
package com.JSR.PharmaFlow.Filters;

import com.JSR.PharmaFlow.Utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        log.debug("🔐 JwtFilter checking: {} {}", method, path);

        if (shouldSkipJwtFilter(path, method)) {
            log.debug("🔐 Skipping JWT filter for: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                String username = jwtUtil.extractUsername(jwt);
                log.info("🔐 Authenticated user from JWT: {}", username);

                List<String> roles = jwtUtil.extractRoles(jwt);
                log.info("🔐 Roles from JWT: {}", roles);

                List<GrantedAuthority> authorities = roles != null
                        ? roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                        : new ArrayList<>();

                log.info("🔐 Authorities set: {}", authorities);

                // ✅ Create a UserDetails object for principal
                UserDetails userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(username)
                        .password("") // password not needed here
                        .authorities(authorities)
                        .build();

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,   // set principal as UserDetails
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.info("🔐 Authentication set in SecurityContext");

            } else {
                log.warn("🔐 Invalid or expired JWT token or not present");
            }

        } catch (Exception e) {
            log.error("🔐 JWT Filter error: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipJwtFilter(String path, String method) {
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        return path.equals("/api/auth/signin") ||
                path.equals("/api/auth/signup") ||
                path.startsWith("/api/reset/") ||
                path.startsWith("/api/test/") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/api/medicines/getMedicines") ||
                path.startsWith("/api/medicines/test") ||
                path.startsWith("/api/medicines/add") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/") ||
                path.startsWith("/api/health/") ||
                path.startsWith("/check/") ||
                path.startsWith("/api/ollama/") ||
                path.startsWith("/api/public/med/") ||
                path.startsWith("/api/payment/");
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            log.debug("🔐 Parsed JWT token, length: {}", token.length());
            return token;
        }
        log.debug("🔐 No Authorization header found");
        return null;
    }
}
