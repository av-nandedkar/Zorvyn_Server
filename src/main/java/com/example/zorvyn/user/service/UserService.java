package com.example.zorvyn.user.service;

import com.example.zorvyn.common.exception.ConflictException;
import com.example.zorvyn.common.exception.ResourceNotFoundException;
import com.example.zorvyn.common.model.RoleType;
import com.example.zorvyn.common.model.UserStatus;
import com.example.zorvyn.user.dto.CreateUserRequest;
import com.example.zorvyn.user.dto.UserResponse;
import com.example.zorvyn.user.entity.AppUser;
import com.example.zorvyn.user.repository.AppUserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        validateUniqueEmail(normalizedEmail);

        AppUser user = new AppUser();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getUserOrThrow(id));
    }

    @Transactional
    public UserResponse updateRole(Long id, RoleType role) {
        AppUser user = getUserOrThrow(id);
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateStatus(Long id, UserStatus status) {
        AppUser user = getUserOrThrow(id);
        user.setStatus(status);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = getUserOrThrow(id);
        userRepository.delete(user);
    }

    public AppUser getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id " + id));
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A user with this email address already exists.");
        }
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}


