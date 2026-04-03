package com.example.zorvyn.auth.repository;

import com.example.zorvyn.auth.entity.AuthSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {

    Optional<AuthSession> findByIdAndTokenHashAndRevokedAtIsNull(String id, String tokenHash);
}

