package com.echoowl.backend.repository;

import com.echoowl.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByExternalId(String externalId);

    Optional<User> findByApiKey(String apiKey);

    boolean existsByEmail(String email);
}
