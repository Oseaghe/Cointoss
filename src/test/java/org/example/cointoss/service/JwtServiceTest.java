package org.example.cointoss.service;

import io.jsonwebtoken.security.Keys;
import org.example.cointoss.config.JwtConfig;
import org.example.cointoss.entities.Role;
import org.example.cointoss.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;
    private JwtConfig jwtConfig;
    private SecretKey secretKey;
    private User user;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        // Use a 32+ char string for HS256
        jwtConfig.setSecret("0123456789abcdef0123456789abcdef");
        jwtConfig.setAccessTokenExpiration(2); // 2 seconds
        jwtConfig.setRefreshTokenExpiration(5); // 5 seconds
        jwtService = new JwtService(jwtConfig);
        user = new User();
        user.setId(42L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
    }

    @Test
    void generateAndParseAccessToken_success() {
        Jwt jwt = jwtService.generateAccessToken(user);
        assertNotNull(jwt);
        assertFalse(jwt.isExpired());
        assertEquals(user.getId(), jwt.getUserId());
        assertEquals(user.getRole(), jwt.getRole());
        assertTrue(jwt.toString().length() > 10);
    }

    @Test
    void parseToken_returnsNullForInvalidToken() {
        Jwt jwt = jwtService.parseToken("invalid.token.value");
        assertNull(jwt);
    }

    @Test
    void isExpired_returnsTrueForExpiredToken() throws InterruptedException {
        Jwt jwt = jwtService.generateAccessToken(user);
        assertNotNull(jwt);
        Thread.sleep(2100); // Wait for token to expire (2.1s)
        assertTrue(jwt.isExpired());
    }

    @Test
    void generateAndParseRefreshToken_success() {
        Jwt jwt = jwtService.generateRefreshToken(user);
        assertNotNull(jwt);
        assertFalse(jwt.isExpired());
        assertEquals(user.getId(), jwt.getUserId());
        assertEquals(user.getRole(), jwt.getRole());
    }
}
