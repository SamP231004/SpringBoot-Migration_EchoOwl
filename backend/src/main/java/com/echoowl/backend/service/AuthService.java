package com.echoowl.backend.service;

import com.echoowl.backend.dto.AuthUser;
import com.echoowl.backend.model.User;
import com.echoowl.backend.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {
    private final UserRepository users;
    private final ClerkService clerkService;

    public AuthService(UserRepository users, ClerkService clerkService) {
        this.users = users;
        this.clerkService = clerkService;
    }

    public User requireUser(HttpHeaders headers) {
        String bearer = clerkService.bearer(headers);
        if (bearer != null) {
            Optional<User> apiKeyUser = users.findByApiKey(bearer);
            if (apiKeyUser.isPresent()) {
                return apiKeyUser.get();
            }
        }

        return clerkService.fromHeaders(headers)
                .map(this::syncUser)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Unauthorized"));
    }

    public Optional<AuthUser> currentClerkUser(HttpHeaders headers) {
        return clerkService.fromHeaders(headers);
    }

    public User syncUser(AuthUser authUser) {
        return users.findByExternalId(authUser.externalId()).orElseGet(() -> {
            User user = new User();
            user.setExternalId(authUser.externalId());
            user.setEmail(uniqueEmail(authUser));
            user.setQuotaLimit(100);
            return users.save(user);
        });
    }

    private String uniqueEmail(AuthUser authUser) {
        String email = authUser.email();
        if (!users.existsByEmail(email)) {
            return email;
        }

        int at = email.indexOf('@');
        String local = at > 0 ? email.substring(0, at) : email;
        String domain = at > 0 ? email.substring(at) : "@unknown.local";
        return local + "+" + authUser.externalId() + domain;
    }
}
