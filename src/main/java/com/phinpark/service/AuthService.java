package com.phinpark.service;

import com.phinpark.dto.RegisterRequest;
import com.phinpark.dto.UserResponse;
import com.phinpark.entity.UserEntity;
import com.phinpark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Value("${phinparks.admin.setup-key:}")
    private String adminSetupKey;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserResponse login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be null or empty");
        }

        Optional<UserEntity> userOpt = userRepository.findByEmailIgnoreCase(email.trim());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        UserEntity user = userOpt.get();
        String storedPassword = user.getPassword();
        boolean passwordMatches = storedPassword != null && storedPassword.startsWith("$2")
                ? passwordEncoder.matches(password, storedPassword)
                : password.equals(storedPassword);
        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password");
        }

        if (storedPassword == null || !storedPassword.startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        return convertToUserResponse(user);
    }

    public UserResponse register(RegisterRequest request) {
        if (request == null) {
            throw new RuntimeException("Registration request cannot be null");
        }
        if (request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        if (request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email cannot be null or empty");
        }

        String normalizedEmail = request.getEmail().trim();
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        UserEntity newUser = new UserEntity(
                request.getFirstName(),
                request.getLastName(),
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                "CLIENT");

        UserEntity savedUser = userRepository.save(newUser);
        return convertToUserResponse(savedUser);
    }

    public synchronized UserResponse createFirstAdmin(RegisterRequest request) {
        if (adminSetupKey == null || adminSetupKey.length() < 32) {
            throw new RuntimeException(
                    "Initial admin setup is disabled. Configure PHINPARKS_ADMIN_SETUP_KEY with at least 32 characters.");
        }
        if (request == null || request.getSetupKey() == null
                || !MessageDigest.isEqual(adminSetupKey.getBytes(StandardCharsets.UTF_8),
                        request.getSetupKey().getBytes(StandardCharsets.UTF_8))) {
            throw new RuntimeException("Invalid admin setup key");
        }
        if (userRepository.countByAccountTypeIgnoreCase("ADMIN") > 0) {
            throw new RuntimeException("An admin account already exists");
        }
        if (request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        if (request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()
                || request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new RuntimeException("First name and last name are required");
        }

        String normalizedEmail = request.getEmail() == null ? "" : request.getEmail().trim();
        if (normalizedEmail.isEmpty()) {
            throw new RuntimeException("Email cannot be null or empty");
        }
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        UserEntity admin = new UserEntity(
                request.getFirstName().trim(),
                request.getLastName().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                "ADMIN");
        return convertToUserResponse(userRepository.save(admin));
    }

    private UserResponse convertToUserResponse(UserEntity user) {
        if (user == null) {
            throw new RuntimeException("User cannot be null");
        }
        String userType = "CLIENT";
        if ("ADMIN".equalsIgnoreCase(user.getAccountType())) {
            userType = "ADMIN";
        }
        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
        return new UserResponse(
                user.getId() == null ? 0 : user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                fullName,
                userType);
    }
}
