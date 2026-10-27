package com.studenterp;

import com.studenterp.dto.LoginRequest;
import com.studenterp.dto.RegisterRequest;
import com.studenterp.dto.AuthResponse;
import com.studenterp.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthTest {

    @Autowired
    private AuthService authService;

    @Test
    void testRegisterAndLogin() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole("STUDENT");

        AuthResponse registerResponse = authService.register(registerRequest);
        assertNotNull(registerResponse.getToken());
        assertEquals("testuser", registerResponse.getUsername());
        assertEquals("STUDENT", registerResponse.getRole());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        AuthResponse loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse.getToken());
        assertEquals("testuser", loginResponse.getUsername());
    }

    @Test
    void testDuplicateUsernameFails() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicate");
        request.setEmail("dup1@example.com");
        request.setPassword("password123");
        request.setRole("STUDENT");
        authService.register(request);

        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("duplicate");
        request2.setEmail("dup2@example.com");
        request2.setPassword("password123");
        request2.setRole("STUDENT");

        assertThrows(IllegalArgumentException.class, () -> authService.register(request2));
    }

    @Test
    void testInvalidLoginFails() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("wrong");

        assertThrows(Exception.class, () -> authService.login(request));
    }
}
