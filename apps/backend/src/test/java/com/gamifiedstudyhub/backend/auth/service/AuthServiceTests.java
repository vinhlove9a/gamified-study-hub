package com.gamifiedstudyhub.backend.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.dto.LoginRequest;
import com.gamifiedstudyhub.backend.auth.dto.RegisterRequest;
import com.gamifiedstudyhub.backend.auth.mapper.AuthMapper;
import com.gamifiedstudyhub.backend.auth.security.JwtProperties;
import com.gamifiedstudyhub.backend.auth.security.JwtService;
import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.user.entity.UserStatus;
import com.gamifiedstudyhub.backend.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-at-least-32-characters-long");
        jwtProperties.setAccessTokenExpirationMinutes(60);
        jwtService = new JwtService(jwtProperties);
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                new AuthMapper(),
                new PasswordPolicyValidator()
        );
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("Test User", "  Test@Example.com ", "password123");

        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.fromString("6cc8282f-b66f-4fe4-bf7f-dd6f6f90420f"));
            return user;
        });

        AuthResponse response = authService.register(request);

        assertNotNull(response.accessToken());
        assertTrue(jwtService.isAccessTokenValid(response.accessToken()));
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresInSeconds());
        assertEquals("test@example.com", response.user().email());
        assertEquals("Test User", response.user().fullName());
        assertEquals("ACTIVE", response.user().status());
        assertNotNull(response.user().id());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("test@example.com", savedUser.getEmail());
        assertNotEquals("password123", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPasswordHash()));
    }

    @Test
    void register_shouldFailWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");

        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("test@example.com")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));

        assertEquals(ErrorCodes.EMAIL_ALREADY_EXISTS, exception.getCode());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.fromString("3afb3035-2507-482e-8ec6-79cffb3f2f5b"));
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setFullName("Test User");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("test@example.com"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.login(new LoginRequest(" Test@example.com ", "password123"));

        assertNotNull(response.accessToken());
        assertTrue(jwtService.isAccessTokenValid(response.accessToken()));
        assertEquals("test@example.com", response.user().email());
        assertEquals("ACTIVE", response.user().status());
        assertNotNull(user.getLastLoginAt());
        verify(userRepository).save(user);
    }

    @Test
    void login_shouldReturnGenericErrorForInvalidCredentials() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("test@example.com"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(new LoginRequest("test@example.com", "wrongpassword"))
        );

        assertEquals(ErrorCodes.INVALID_CREDENTIALS, exception.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Invalid email or password", exception.getMessage());
    }
}
