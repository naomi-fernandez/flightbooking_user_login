package com.userlogin.flightbooking.config;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.Authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import com.userlogin.flightbooking.service.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;


import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)


            throws ServletException, IOException {
        System.out.println(" JwtAuthenticationFilter is ACTIVE for request: " + request.getRequestURI());
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String jwt = authHeader.substring(7);
            System.out.println("JWT token extracted: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
            
            final String userEmail = jwtService.extractUsername(jwt);
            System.out.println("User email from token: " + userEmail);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Current authentication: " + authentication);
            
            if (userEmail != null && authentication == null) {
                System.out.println("Loading user details for: " + userEmail);
                try {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                    System.out.println("User details loaded successfully: " + userDetails.getUsername());
                    
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        System.out.println("JWT token is VALID");
                        String role=jwtService.extractUserRole(jwt);
                        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_"+role));
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities
                        );
                        // temp code
                        System.out.println("Authorities from token: " + authorities);
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        System.out.println("JWT token is INVALID");
                    }
                } catch (Exception e) {
                    System.out.println("Failed to load user: " + e.getMessage());
                }
            } else {
                System.out.println("UserEmail is null or authentication already exists");
            }


            filterChain.doFilter(request, response);
        }catch (Exception exception){
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }

}