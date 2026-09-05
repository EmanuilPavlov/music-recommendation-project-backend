package com.example.student_system.musicproject.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("FILTER HIT: {}", path);

        if (path.startsWith("/api/auth/")) {
            log.info("Skipping filter for auth endpoint");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHeader != null ? "Present" : "Missing");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                log.info("Verifying token...");
                FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);

                String uid = firebaseToken.getUid();
                String email = firebaseToken.getEmail();

                log.info("Authenticated user: {} ({})", email, uid);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                uid,
                                null,
                                Collections.emptyList()
                        );
                authentication.setDetails(email);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                request.setAttribute("firebaseUid", uid);
                request.setAttribute("firebaseEmail", email);

            } catch (Exception e) {
                log.error("Invalid Firebase token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid Firebase Token");
                return;
            }
        } else {
            log.warn("No Authorization header found for path: {}", path);
        }

        filterChain.doFilter(request, response);
    }
}