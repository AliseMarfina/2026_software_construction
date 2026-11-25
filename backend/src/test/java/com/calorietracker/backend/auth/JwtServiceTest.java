package com.calorietracker.backend.auth;
import com.calorietracker.backend.config.JwtProperties;
import com.calorietracker.backend.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class JwtServiceTest {
    @Test void encodesSubjectAndExpiration() {
        JwtEncoder encoder = mock(JwtEncoder.class);
        Jwt token = mock(Jwt.class);
        when(token.getTokenValue()).thenReturn("signed-token");
        when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(token);
        User user = new User(); user.setId(42L);
        assertEquals("signed-token", new JwtService(encoder, new JwtProperties("test", 3600)).generateToken(user));
        var captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(encoder).encode(captor.capture());
        var claims = captor.getValue().getClaims();
        assertEquals("42", claims.getSubject());
        assertEquals(3600, Duration.between(claims.getIssuedAt(), claims.getExpiresAt()).getSeconds());
    }
}
