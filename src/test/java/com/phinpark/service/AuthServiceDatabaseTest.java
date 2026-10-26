package com.phinpark.service;

import com.phinpark.dto.RegisterRequest;
import com.phinpark.dto.UserResponse;
import com.phinpark.entity.UserEntity;
import com.phinpark.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceDatabaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldPersistUserInRepository() {
        when(userRepository.findByEmailIgnoreCase("client@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(101);
            return user;
        });

        RegisterRequest request = new RegisterRequest(
                "Jane",
                "Doe",
                "client@example.com",
                "password123",
                "password123",
                "ADMIN");

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("CLIENT", response.getUserType());
        ArgumentCaptor<UserEntity> savedUser = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("CLIENT", savedUser.getValue().getAccountType());
        assertNotEquals("password123", savedUser.getValue().getPassword());
        assertTrue(savedUser.getValue().getPassword().startsWith("$2"));
    }

    @Test
    void setupShouldCreateOnlyTheFirstAdminWithHashedPassword() {
        ReflectionTestUtils.setField(authService, "adminSetupKey", "a-unique-setup-secret-with-32-characters");
        when(userRepository.countByAccountTypeIgnoreCase("ADMIN")).thenReturn(0L);
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(102);
            return user;
        });

        RegisterRequest request = new RegisterRequest(
                "Alex",
                "Admin",
                "admin@example.com",
                "securePass123",
                "securePass123",
                "CLIENT");
        request.setSetupKey("a-unique-setup-secret-with-32-characters");

        UserResponse response = authService.createFirstAdmin(request);

        ArgumentCaptor<UserEntity> savedUser = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("ADMIN", response.getUserType());
        assertEquals("ADMIN", savedUser.getValue().getAccountType());
        assertTrue(savedUser.getValue().getPassword().startsWith("$2"));
    }

    @Test
    void setupShouldRefuseToCreateAnotherAdmin() {
        ReflectionTestUtils.setField(authService, "adminSetupKey", "a-unique-setup-secret-with-32-characters");
        when(userRepository.countByAccountTypeIgnoreCase("ADMIN")).thenReturn(1L);

        RegisterRequest request = new RegisterRequest(
                "Alex",
                "Admin",
                "admin@example.com",
                "securePass123",
                "securePass123",
                null);
        request.setSetupKey("a-unique-setup-secret-with-32-characters");

        assertThrows(RuntimeException.class, () -> authService.createFirstAdmin(request));
    }
}
