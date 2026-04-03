package com.example.zorvyn.auth.controller;

import com.example.zorvyn.auth.dto.CurrentUserResponse;
import com.example.zorvyn.auth.dto.LoginRequest;
import com.example.zorvyn.auth.dto.LoginResponse;
import com.example.zorvyn.auth.dto.RegisterRequest;
import com.example.zorvyn.auth.security.AppUserPrincipal;
import com.example.zorvyn.auth.service.AuthService;
import com.example.zorvyn.user.entity.AppUser;
import javax.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authorizationHeader) {
        String rawToken = parseRawToken(authorizationHeader);
        authService.logout(rawToken);
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(@AuthenticationPrincipal AppUserPrincipal principal) {
        AppUser user = principal.getUser();
        return authService.toCurrentUser(user);
    }

    private String parseRawToken(String authorizationHeader) {
        return authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader;
    }
}

