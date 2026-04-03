package com.example.zorvyn.auth.service;

import com.example.zorvyn.auth.dto.AuthToken;
import com.example.zorvyn.auth.dto.CurrentUserResponse;
import com.example.zorvyn.auth.dto.LoginRequest;
import com.example.zorvyn.auth.dto.LoginResponse;
import com.example.zorvyn.auth.dto.RegisterRequest;
import com.example.zorvyn.auth.dto.TokenPayload;
import com.example.zorvyn.auth.entity.AuthSession;
import com.example.zorvyn.auth.repository.AuthSessionRepository;
import com.example.zorvyn.common.exception.ConflictException;
import com.example.zorvyn.auth.security.AppUserPrincipal;
import com.example.zorvyn.common.model.RoleType;
import com.example.zorvyn.common.exception.ResourceNotFoundException;
import com.example.zorvyn.common.model.UserStatus;
import com.example.zorvyn.user.entity.AppUser;
import com.example.zorvyn.user.repository.AppUserRepository;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            AppUserRepository appUserRepository,
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        this.appUserRepository = appUserRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No user found for the provided email."));

        validateActiveUser(user);
        validatePassword(request.getPassword(), user.getPasswordHash());

        AuthToken token = tokenService.generateToken(user.getId(), user.getRole());
        persistSession(user, token);
        return new LoginResponse(token.getRawToken(), token.getExpiresAt(), toCurrentUser(user));
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("A user with this email address already exists.");
        }

        AppUser user = new AppUser();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleType.VIEWER);
        user.setStatus(UserStatus.ACTIVE);
        AppUser saved = appUserRepository.save(user);

        AuthToken token = tokenService.generateToken(saved.getId(), saved.getRole());
        persistSession(saved, token);

        return new LoginResponse(token.getRawToken(), token.getExpiresAt(), toCurrentUser(saved));
    }

    @Transactional(readOnly = true)
    public AppUserPrincipal authenticateByToken(String rawToken) {
        TokenPayload payload = tokenService.parseAndValidate(rawToken);
        String tokenHash = tokenService.hashToken(rawToken);

        AuthSession session = authSessionRepository
                .findByIdAndTokenHashAndRevokedAtIsNull(payload.getSessionId(), tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Active session not found for the provided token."));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Your session has expired. Please sign in again.");
        }

        AppUser user = session.getUser();
        validateActiveUser(user);

        return new AppUserPrincipal(user);
    }

    @Transactional
    public void logout(String rawToken) {
        TokenPayload payload = tokenService.parseAndValidate(rawToken);
        String tokenHash = tokenService.hashToken(rawToken);

        AuthSession session = authSessionRepository
                .findByIdAndTokenHashAndRevokedAtIsNull(payload.getSessionId(), tokenHash)
                .orElse(null);

        if (session != null) {
            session.setRevokedAt(Instant.now());
            authSessionRepository.save(session);
        }
    }

    public CurrentUserResponse toCurrentUser(AppUser user) {
        return new CurrentUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus());
    }

    private void validateActiveUser(AppUser user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Your account is inactive. Please contact an administrator.");
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IllegalArgumentException("Invalid credentials.");
        }
    }

    private void persistSession(AppUser user, AuthToken token) {
        AuthSession session = new AuthSession();
        session.setId(token.getSessionId());
        session.setUser(user);
        session.setSalt(token.getSalt());
        session.setTokenHash(token.getTokenHash());
        session.setExpiresAt(token.getExpiresAt());
        authSessionRepository.save(session);
    }
}


