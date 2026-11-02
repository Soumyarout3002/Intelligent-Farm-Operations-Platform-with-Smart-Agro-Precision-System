
package com.backend.security;

import com.backend.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        // Only controller methods can declare role restrictions.
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        RequireRole requirement =
                method.getMethodAnnotation(RequireRole.class);

        // Also support restricting an entire controller class.
        if (requirement == null) {
            requirement =
                    method.getBeanType().getAnnotation(RequireRole.class);
        }

        // Endpoints without the annotation keep their existing access rules.
        if (requirement == null) {
            return true;
        }

        Object value = request.getAttribute("authenticatedUser");

        if (!(value instanceof User user)) {
            return writeError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication is required."
            );
        }

        String role = user.getRole() == null
                ? ""
                : user.getRole().trim().toUpperCase(Locale.ROOT);

        boolean allowed = Arrays.stream(requirement.value())
                .anyMatch(allowedRole ->
                        allowedRole.equalsIgnoreCase(role));

        if (!allowed) {
            return writeError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to access this resource."
            );
        }

        return true;
    }

    private boolean writeError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"message\":\"" + message + "\"}"
        );

        return false;
    }
}
