package com.deusto.coffeestack.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "SuperSecretKeyParaTestsUnitarios1234567890!");
        ReflectionTestUtils.setField(jwtService, "expiration", 1000L * 60 * 60); // 1 hora

        userDetails = new User(
                "testuser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_EMPLEADO"))
        );
    }

    @Test
    void generateToken_creaTokenConClaims() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_devuelveUsernameCorrecto() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void isTokenValid_tokenValido_devuelveTrue() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_otroUser_devuelveFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otroUser = new User("otro", "pass", List.of());
        assertFalse(jwtService.isTokenValid(token, otroUser));
    }
    
    @Test
    void isTokenValid_tokenExpiradoOInvalido_devuelveFalse() {
        assertFalse(jwtService.isTokenValid("token-invalido-o-caducado", userDetails));
    }
}
