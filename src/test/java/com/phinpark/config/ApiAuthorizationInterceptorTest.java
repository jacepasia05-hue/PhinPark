package com.phinpark.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phinpark.entity.UserEntity;
import com.phinpark.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

class ApiAuthorizationInterceptorTest {

    private UserRepository userRepository;
    private ApiAuthorizationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        interceptor = new ApiAuthorizationInterceptor(userRepository, new ObjectMapper());
    }

    @Test
    void rejectsUnauthenticatedApiRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/parking-slots");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void rejectsClientCallingAdminEndpointEvenWithSession() throws Exception {
        UserEntity client = new UserEntity("Jane", "Doe", "jane@example.test", "password", "CLIENT");
        client.setId(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(client));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/parking-slots");
        HttpSession session = request.getSession();
        session.setAttribute("userId", 7);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    void allowsAdminCallingAdminEndpoint() throws Exception {
        UserEntity admin = new UserEntity("Alex", "Admin", "admin@example.test", "password", "ADMIN");
        admin.setId(8);
        when(userRepository.findById(8)).thenReturn(Optional.of(admin));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/parking-slots");
        request.getSession().setAttribute("userId", 8);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(200, response.getStatus());
    }

    @Test
    void rejectsClientTryingToRecordCashAndCheckOut() throws Exception {
        UserEntity client = new UserEntity("Jane", "Doe", "jane@example.test", "password", "CLIENT");
        client.setId(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(client));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/tickets/12/checkout");
        request.getSession().setAttribute("userId", 7);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    void allowsFirstAdminSetupWithoutAnExistingSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/setup-admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}
