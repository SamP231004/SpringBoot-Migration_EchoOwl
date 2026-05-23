package com.echoowl.backend.controller;

import com.echoowl.backend.service.EventIngestionService;
import com.echoowl.backend.util.InputParser;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventIngestionService events;
    private final InputParser inputParser;

    public EventController(EventIngestionService events, InputParser inputParser) {
        this.events = events;
        this.inputParser = inputParser;
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public Map<String, Object> ingest(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> body) {
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
        }
        if (!authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid auth header format. Expected: 'Bearer [API_KEY]'");
        }

        String apiKey = authorization.substring("Bearer ".length()).trim();
        if (apiKey.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid API key");
        }

        Map<String, Object> input = inputParser.parseMap(body);
        String category = inputParser.requiredString(input, "category");
        Object rawFields = input.getOrDefault("fields", Map.of());
        if (!(rawFields instanceof Map<?, ?> fields)) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "fields must be an object");
        }

        return events.ingest(
                apiKey,
                category,
                (Map<String, Object>) fields,
                input.get("description") == null ? null : input.get("description").toString()
        );
    }
}
