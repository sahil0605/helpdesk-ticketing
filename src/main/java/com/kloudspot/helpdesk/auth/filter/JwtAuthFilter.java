package com.kloudspot.helpdesk.auth.filter;

import com.kloudspot.helpdesk.auth.model.AuthUser;
import com.kloudspot.helpdesk.auth.service.CustomUserDetailsService;
import com.kloudspot.helpdesk.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                applyToken(header.substring(7), request);
            } catch (Exception ex) {
                log.warn("Invalid JWT on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private void applyToken(String token, HttpServletRequest request) {
        if (!jwtUtil.validateToken(token) || SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        String email = jwtUtil.extractEmail(token);
        AuthUser user = (AuthUser) userDetailsService.loadUserByUsername(email);
        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
