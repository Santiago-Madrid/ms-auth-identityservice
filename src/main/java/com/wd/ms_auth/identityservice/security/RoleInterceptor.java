package com.wd.ms_auth.identityservice.security;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        // Buca la anotacion @RequireRole en el método
        RequireRole annotation = method.getMethodAnnotation(RequireRole.class);

        // Si no está en el método, la busca en la clase del controller
        if (annotation == null) {
            annotation = method.getBeanType().getAnnotation(RequireRole.class);
        }

        if (annotation == null) {
            return true; // No nos importa
        }

        Object rol = request.getAttribute("role");

        if (!(rol instanceof Long rolid)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Usuario no autenticado con rol\"}");
            return false;
        }

        boolean hasRole = Arrays.stream(annotation.value()).anyMatch(role -> role.getId() == rolid);

        if (!hasRole) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"No tienes permisos para relizar esta acción\"}");
            return false;
        }

        return true;
    }
}

