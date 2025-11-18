//package com.JSR.PharmaFlow.Filters;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import com.JSR.PharmaFlow.Utils.JwtUtil;
//
//import java.io.IOException;
//
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//    private static final Logger logger = LoggerFactory.getLogger( JwtFilter.class );
//    private final JwtUtil jwtUtil;
//    private final UserDetailsService userDetailsService;
//
//    public JwtFilter( JwtUtil jwtUtil, UserDetailsService userDetailsService ) {
//        this.jwtUtil = jwtUtil;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Override
//    protected void doFilterInternal( HttpServletRequest request,
//                                     HttpServletResponse response,
//                                     FilterChain filterChain ) throws ServletException, IOException {
//        // Skip JWT checks for OPTIONS requests
//
//
//        String path = request.getServletPath();
//
//        // Skip JWT check for auth endpoints and OPTIONS requests
////        if (request.getServletPath().startsWith( "/api/auth/signup" ) ||
////                "OPTIONS".equalsIgnoreCase( request.getMethod() )) {
////            filterChain.doFilter( request, response );
////            return;
////        }
//
//        if (path.equals("/api/auth/signin") ||
//                path.equals("/api/auth/signup") ||
//                path.equals ( ("/oauth2/") ) ||
//                path.equals ( "/login/oauth2/" )||
//                path.startsWith("/api/reset/") || // ADD THIS LINE - Skip JWT for password reset endpoint
//                "OPTIONS".equalsIgnoreCase(request.getMethod()))
//        {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//
//
//
//        try {
//            String jwt = parseJwt( request );
//            if (jwt != null && jwtUtil.validateToken( jwt )) {
//                String username = jwtUtil.extractUsername( jwt );
//                UserDetails userDetails = userDetailsService.loadUserByUsername( username );
//
//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities() );
//                authentication.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );
//
//                SecurityContextHolder.getContext().setAuthentication( authentication );
//            }
//        } catch (Exception e) {
//            logger.error( "Cannot set user authentication: {}", e.getMessage() );
//        }
//
//        filterChain.doFilter( request, response );
//    }
//
//    // Add this method to parse JWT from Authorization header
//    private String parseJwt( HttpServletRequest request ) {
//        String headerAuth = request.getHeader( "Authorization" );
//
//        if (StringUtils.hasText( headerAuth ) && headerAuth.startsWith( "Bearer " )) {
//            return headerAuth.substring( 7 ); // Extract the token after "Bearer "
//        }
//
//        return null;
//    }
//}
//
////Summary
////The JwtFilter is used to:
////
////Validate JWTs in the Authorization header for protected endpoints.
////Authenticate users by setting the Spring Security context with UserDetails and authorities.
////Skip validation for /api/auth endpoints (e.g., login, refresh) and OPTIONS requests.
////Work alongside JwtAuthenticationFilter to secure your application by verifying tokens issued during login.
//
//
////Yes, you should include your OAuth-related path(s) in that conditional check
////    if you want to allow unauthenticated access to them—especially if this
////code is inside a custom filter like a OncePerRequestFilter for JWT validation.
////
////For example, if your OAuth path is something like /oauth2/authorization/google
////or /login/oauth2/code/google, and you want users to access it without being blocked by JWT filter logic, add it to the condition:
//
//



package com.JSR.PharmaFlow.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.JSR.PharmaFlow.Utils.JwtUtil;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        String method = request.getMethod();

        logger.debug("JwtFilter checking: {} {}", method, path);

        // Skip JWT filter for public endpoints
        if (shouldSkipJwtFilter(path, method)) {
            logger.debug("Skipping JWT filter for: {} {}", method, path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                String username = jwtUtil.extractUsername(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipJwtFilter(String path, String method) {
        // Skip for OPTIONS requests (preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Skip for public endpoints
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/forgot-password") ||
                path.startsWith("/api/reset/") ||  // ALL reset endpoints
                path.startsWith("/api/test/") ||   // ALL test endpoints
                path.startsWith("/api/public/") ||
                path.startsWith("api/auth/signin")||
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
            return headerAuth.substring(7); // Extract the token after "Bearer "
        }

        return null;
    }
}