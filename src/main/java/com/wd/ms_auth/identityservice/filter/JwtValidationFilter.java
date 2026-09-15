package com.wd.ms_auth.identityservice.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.wd.ms_auth.identityservice.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        log.info("REQUEST URI -> {}", path);

        // Usar contains para cubrir rutas con prefijo como /api/v1/auth o /api/v1/actuator
        return path.contains("/auth")
            || path.contains("/actuator")
            || path.contains("/error")
            || isInternalUserLookup(path);
    }

    /**
     * /users/{id} (numérico) y /users/by-email son endpoints internos, sin
     * @RequireRole, pensados para llamadas service-to-service (ej. ms-enrollment
     * resolviendo datos de participantes o de la cuenta del agente de IA) que no
     * llevan JWT. No se excluye todo /users/** para no romper el @RequireRole de
     * /users/document/{documentNumber} ni /users/update.
     */
    private boolean isInternalUserLookup(String path) {
        if (path.endsWith("/users/by-email")) {
            return true;
        }
        String usersPrefix = "/users/";
        int idx = path.lastIndexOf(usersPrefix);
        if (idx == -1) {
            return false;
        }
        String remainder = path.substring(idx + usersPrefix.length());
        return remainder.matches("\\d+");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authorization header missing\"}");
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token)) {
                String username = jwtService.extractEmail(token);
                Long userId = jwtService.extractUserId(token);
                Long roleId = jwtService.extractRoleId(token);

                request.setAttribute("username", username);
                request.setAttribute("userId", userId);
                request.setAttribute("role", roleId); // Necesario para RoleInterceptor
                
                filterChain.doFilter(request, response);

            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Token is invalid or expired\"}");
            }
        } catch (Exception e) {
            log.error("Error JWT", e);

            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}