package com.phinpark.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phinpark.entity.UserEntity;
import com.phinpark.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class ApiAuthorizationInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ApiAuthorizationInterceptor(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (isPublicRequest(path, method)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Object sessionUserId = session == null ? null : session.getAttribute("userId");
        if (!(sessionUserId instanceof Integer userId)) {
            return reject(response, HttpStatus.UNAUTHORIZED, "Please log in");
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            session.invalidate();
            return reject(response, HttpStatus.UNAUTHORIZED, "Please log in again");
        }

        boolean admin = "ADMIN".equalsIgnoreCase(user.getAccountType());
        if (isAdminOnlyRequest(path, method) && !admin) {
            return reject(response, HttpStatus.FORBIDDEN, "Administrator access required");
        }

        request.setAttribute("authenticatedUser", user);
        return true;
    }

    private boolean isPublicRequest(String path, String method) {
        if ("POST".equals(method) && (path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/setup-admin")
                || path.equals("/api/auth/logout"))) {
            return true;
        }
        return "GET".equals(method)
                && (path.equals("/api/parking-slots") || path.equals("/api/parking-slots/types"));
    }

    private boolean isAdminOnlyRequest(String path, String method) {
        return ("POST".equals(method) && path.equals("/api/parking-slots"))
                || ("POST".equals(method) && path.matches("/api/tickets/\\d+/checkout"))
                || ("GET".equals(method) && (path.equals("/api/vehicles")
                        || path.equals("/api/tickets/active") || path.equals("/api/tickets/history")))
                || ("DELETE".equals(method) && path.startsWith("/api/vehicles/"));
    }

    private boolean reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "success", false,
                "message", message,
                "statusCode", status.value()));
        return false;
    }
}
