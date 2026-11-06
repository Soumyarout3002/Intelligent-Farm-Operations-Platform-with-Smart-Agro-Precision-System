
package com.backend.security;

import com.backend.entity.AuthSession;
import com.backend.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;

    public AuthInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        // Allow browser CORS preflight requests without authentication.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
            
           // Only the intended POST authentication endpoints are public.
                String method = request.getMethod();

                if ("POST".equalsIgnoreCase(method)
                        && (path.equals("/api/auth/login")
                        || path.equals("/api/auth/register")
                        || path.equals("/api/auth/forgot-password")
                        || path.equals("/api/auth/reset-password")
                        || path.equals("/api/dev/password-reset/link")
                        || path.equals("/api/dev/password-reset/token"))) {
                    return true;
                }


        // Protected API calls must provide a Bearer token.
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return unauthorized(response, "Bearer token is required.");
        }

        String token = header.substring(7).trim();

        // Check that the session exists, is active and has not expired.
        AuthSession session = sessionService.validateSession(token);

        if (session == null) {
            return unauthorized(response, "Session is invalid or expired.");
        }

        // Controllers can use this attribute to identify the authenticated user.
        request.setAttribute("authenticatedUser", session.getUser());

        return true;
    }

    private boolean unauthorized(
            HttpServletResponse response,
            String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Escape message characters before placing the text in a JSON string.
        String safeMessage = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        response.getWriter().write(
                "{\"message\":\"" + safeMessage + "\"}");

        return false;
    }
}
