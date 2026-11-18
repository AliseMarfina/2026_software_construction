package com.calorietracker.backend.auth;

import com.calorietracker.backend.auth.dto.LoginRequest;
import com.calorietracker.backend.auth.dto.LoginResponse;
import com.calorietracker.backend.auth.dto.MeResponse;
import com.calorietracker.backend.auth.dto.RegisterRequest;
import com.calorietracker.backend.auth.dto.RegisterResponse;
import com.calorietracker.backend.common.NotFoundException;
import com.calorietracker.backend.config.JwtProperties;
import com.calorietracker.backend.user.User;
import com.calorietracker.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        try {
            User saved = userRepository.saveAndFlush(user);
            return new RegisterResponse(saved.getId(), saved.getEmail());
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        return new LoginResponse(jwtService.generateToken(user), jwtProperties.ttlSeconds());
    }

    @Transactional(readOnly = true)
    public MeResponse me(Long userId) {
        return userRepository.findById(userId)
                .map(user -> new MeResponse(user.getId(), user.getEmail()))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
