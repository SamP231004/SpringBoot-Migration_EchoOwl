package com.echoowl.backend.controller;

import com.echoowl.backend.dto.AuthUser;
import com.echoowl.backend.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/getDatabaseSyncStatus")
    public Map<String, Object> getDatabaseSyncStatus(@RequestHeader HttpHeaders headers) {
        return authService.currentClerkUser(headers)
                .map(this::sync)
                .orElseGet(() -> Map.of("isSynced", false));
    }

    private Map<String, Object> sync(AuthUser user) {
        authService.syncUser(user);
        return Map.of("isSynced", true);
    }
}
