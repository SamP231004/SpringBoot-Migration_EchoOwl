package com.echoowl.backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.echoowl.backend.config.AppProperties;
import com.echoowl.backend.dto.AuthUser;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClerkService {
    private final AppProperties properties;
    private final RestClient restClient;

    public ClerkService(AppProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    public Optional<AuthUser> fromHeaders(HttpHeaders headers) {
        String externalId = headers.getFirst("X-Clerk-User-Id");
        String email = headers.getFirst("X-Clerk-User-Email");
        if (externalId != null && email != null) {
            return Optional.of(new AuthUser(externalId, email));
        }

        String bearer = bearer(headers);
        if (bearer == null || bearer.isBlank()) {
            return Optional.empty();
        }

        try {
            DecodedJWT decoded = JWT.decode(bearer);
            String subject = decoded.getSubject();
            if (subject == null) {
                return Optional.empty();
            }
            return fetchUser(subject).or(() -> Optional.of(new AuthUser(subject, subject + "@unknown.local")));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public String bearer(HttpHeaders headers) {
        String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        return auth.substring("Bearer ".length()).trim();
    }

    @SuppressWarnings("unchecked")
    private Optional<AuthUser> fetchUser(String externalId) {
        String secret = properties.clerk().secretKey();
        if (secret == null || secret.isBlank()) {
            return Optional.empty();
        }

        try {
            Map<String, Object> response = restClient.get()
                    .uri("https://api.clerk.com/v1/users/{id}", externalId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + secret)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return Optional.empty();
            }

            Object emailsObject = response.get("email_addresses");
            if (emailsObject instanceof List<?> emails && !emails.isEmpty() && emails.get(0) instanceof Map<?, ?> firstEmail) {
                Object address = firstEmail.get("email_address");
                if (address != null) {
                    return Optional.of(new AuthUser(externalId, address.toString()));
                }
            }
        } catch (Exception ignored) {
            return Optional.empty();
        }

        return Optional.empty();
    }
}
