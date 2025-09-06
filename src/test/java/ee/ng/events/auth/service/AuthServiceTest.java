package ee.ng.events.auth.service;

import ee.ng.events.config.AdminConfig;
import ee.ng.events.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        AdminConfig adminConfig = new AdminConfig();
        adminConfig.setEmail("admin@example.com");
        adminConfig.setPasswordHash(BCrypt.hashpw("secret", BCrypt.gensalt()));

        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setIssuer("test-issuer");
        jwtConfig.setSecret("supersecretkeysupersecretkey123456");
        jwtConfig.setExpiryMinutes(60);

        authService = new AuthService(adminConfig, jwtConfig);
    }

    @Test
    void login_withValidCredentials_returnsJwt() {
        String jwt = authService.login("admin@example.com", "secret");
        assertNotNull(jwt);
        assertEquals(3, jwt.split("\\.").length);
    }

    @Test
    void login_withInvalidPassword_throws() {
        assertThrows(BadCredentialsException.class,
                () -> authService.login("admin@example.com", "wrong"));
    }

    @Test
    void login_withInvalidEmail_throws() {
        assertThrows(BadCredentialsException.class,
                () -> authService.login("notadmin@example.com", "secret"));
    }
}