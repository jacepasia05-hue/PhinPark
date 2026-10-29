package com.phinpark.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.phinpark.dto.ApiResponse;
import com.phinpark.dto.RegisterRequest;
import com.phinpark.dto.UserResponse;
import com.phinpark.service.AuthService;

class AuthControllerAdminSetupTest {

    @Test
    void setupFailureDoesNotExposePrivateConfigurationDetails() {
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", new FailingAuthService());

        ResponseEntity<ApiResponse<UserResponse>> response = controller.setupFirstAdmin(
                new RegisterRequest(), new MockHttpServletRequest());

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Administrator setup is unavailable. Please contact the system administrator.",
                response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("PHINPARKS_ADMIN_SETUP_KEY"));
    }

    private static class FailingAuthService extends AuthService {

        @Override
        public UserResponse createFirstAdmin(RegisterRequest request) {
            throw new RuntimeException(
                    "Initial admin setup is disabled. Configure PHINPARKS_ADMIN_SETUP_KEY with at least 32 characters.");
        }
    }
}
