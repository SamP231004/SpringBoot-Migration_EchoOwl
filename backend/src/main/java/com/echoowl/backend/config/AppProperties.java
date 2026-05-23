package com.echoowl.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "echoowl")
public record AppProperties(
        String appUrl,
        Stripe stripe,
        Discord discord,
        Clerk clerk
) {
    public record Stripe(String secretKey, String webhookSecret, String priceId) {
    }

    public record Discord(String botToken) {
    }

    public record Clerk(String secretKey) {
    }
}
