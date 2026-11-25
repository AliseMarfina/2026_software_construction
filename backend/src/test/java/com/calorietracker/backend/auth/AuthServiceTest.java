package com.calorietracker.backend.auth;
import com.calorietracker.backend.auth.dto.LoginRequest;
import com.calorietracker.backend.config.JwtProperties;
import com.calorietracker.backend.common.NotFoundException;
import com.calorietracker.backend.user.*;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuthServiceTest {
    private final UserRepository repo = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtService jwt = mock(JwtService.class);
    private final AuthService service = new AuthService(repo, encoder, jwt, new JwtProperties("test", 3600));
    private User user() { User u = new User(); u.setId(7L); u.setEmail("test@example.com"); u.setPasswordHash("hash"); return u; }
    @Test void loginNormalizesEmailAndIssuesToken() {
        User u = user();
        when(repo.findByEmail("test@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("password", "hash")).thenReturn(true);
        when(jwt.generateToken(u)).thenReturn("token");
        assertNotNull(service.login(new LoginRequest(" TEST@example.com ", "password")));
        verify(jwt).generateToken(u);
    }
    @Test void rejectsUnknownEmail() {
        when(repo.findByEmail("test@example.com")).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> service.login(new LoginRequest("test@example.com", "password")));
        verifyNoInteractions(jwt, encoder);
    }
    @Test void rejectsWrongPassword() {
        when(repo.findByEmail("test@example.com")).thenReturn(Optional.of(user()));
        when(encoder.matches("wrong", "hash")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> service.login(new LoginRequest("test@example.com", "wrong")));
        verifyNoInteractions(jwt);
    }
    @Test void returnsCurrentUser() { when(repo.findById(7L)).thenReturn(Optional.of(user())); assertNotNull(service.me(7L)); }
    @Test void rejectsMissingUser() { when(repo.findById(7L)).thenReturn(Optional.empty()); assertThrows(NotFoundException.class, () -> service.me(7L)); }
}
