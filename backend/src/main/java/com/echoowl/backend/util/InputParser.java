package com.echoowl.backend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class InputParser {
    private final ObjectMapper objectMapper;

    public InputParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> parseMap(Map<String, ?> raw) {
        Map<String, Object> parsed = new LinkedHashMap<>();
        raw.forEach((key, value) -> parsed.put(key, parseSuperJsonValue(value)));
        return parsed;
    }

    public Object parseSuperJsonValue(Object value) {
        if (!(value instanceof String text)) {
            return value;
        }

        try {
            Object parsed = objectMapper.readValue(text, Object.class);
            if (parsed instanceof Map<?, ?> wrapper && wrapper.containsKey("json")) {
                return wrapper.get("json");
            }
            return parsed;
        } catch (Exception ignored) {
            return value;
        }
    }

    public String requiredString(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value.toString();
    }

    public int requiredInt(Map<String, Object> input, String key) {
        Object value = input.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            return Integer.parseInt(value.toString());
        }
        throw new IllegalArgumentException(key + " is required");
    }

    public Map<String, Object> objectMap(Object value) {
        return objectMapper.convertValue(value, new TypeReference<>() {
        });
    }
}
