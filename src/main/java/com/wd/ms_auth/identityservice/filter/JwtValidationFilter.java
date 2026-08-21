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

    /**
     * Filtro que se ejecuta en cada solicitud para validar el token JWT. Si el
     * token es válido, extrae el username, userId y rolId y los agrega como
     * atributos a la solicitud. Si el token no es válido o ha expirado, devuelve un
     * error 401 Unauthorized con un mensaje de error en formato JSON.
     */
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

            response.getWriter()
                    .write("{\"error\": \"Authorization header missing\"}");

            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token)) {

                String username = jwtService.extractEmail(token);
                Long userId = jwtService.extractUserId(token);

                request.setAttribute("username", username);
                request.setAttribute("userId", userId);
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

    /**
     * Filtra las rutas que no requieren autenticación, en este caso, las rutas de
     * login y registro. Si la ruta es una de estas, el filtro no se ejecuta y la
     * solicitud continúa sin validar el token JWT.
     */

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        log.info("REQUEST URI -> {}", path);

        return path.startsWith("/auth") || path.startsWith("/actuator");
    }

}