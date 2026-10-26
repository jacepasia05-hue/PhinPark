package com.phinpark.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.phinpark.dto.ApiResponse;
import com.phinpark.dto.LoginRequest;
import com.phinpark.dto.RegisterRequest;
import com.phinpark.dto.UserResponse;
import com.phinpark.entity.UserEntity;
import com.phinpark.repository.UserRepository;
import com.phinpark.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is required", HttpStatus.BAD_REQUEST.value(),
                                "Missing email field"));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password is required", HttpStatus.BAD_REQUEST.value(),
                                "Missing password field"));
            }

            UserResponse user = authService.login(request.getEmail(), request.getPassword());
            if (httpRequest.getSession(false) != null) {
                httpRequest.changeSessionId();
            }
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("userId", user.getId());
            return ResponseEntity.ok(ApiResponse.success("Login successful", user, HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), "Authentication failed"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute("userId");
        if (!(userId instanceof Integer id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", HttpStatus.UNAUTHORIZED.value(), "No active session"));
        }
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in again", HttpStatus.UNAUTHORIZED.value(),
                            "Session account no longer exists"));
        }
        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
        UserResponse response = new UserResponse(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), fullName, user.getAccountType());
        return ResponseEntity.ok(ApiResponse.success("Current user", response, HttpStatus.OK.value()));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        try {
            if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("First name is required", HttpStatus.BAD_REQUEST.value(),
                                "Missing firstName field"));
            }
            if (request.getLastName() == null || request.getLastName().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Last name is required", HttpStatus.BAD_REQUEST.value(),
                                "Missing lastName field"));
            }
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is required", HttpStatus.BAD_REQUEST.value(),
                                "Missing email field"));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password is required", HttpStatus.BAD_REQUEST.value(),
                                "Missing password field"));
            }

            UserResponse user = authService.register(request);
            httpRequest.getSession(true).setAttribute("userId", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful", user, HttpStatus.CREATED.value()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
                            "Registration validation failed"));
        }
    }

    @PostMapping("/setup-admin")
    public ResponseEntity<ApiResponse<UserResponse>> setupFirstAdmin(@RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        try {
            UserResponse admin = authService.createFirstAdmin(request);
            httpRequest.getSession(true).setAttribute("userId", admin.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Initial admin account created", admin, HttpStatus.CREATED.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.FORBIDDEN.value(), "Admin setup failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out", null, HttpStatus.OK.value()));
    }
}
