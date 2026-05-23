package com.echoowl.backend.service;

import com.echoowl.backend.config.AppProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DiscordService {
    private final AppProperties properties;
    private final RestClient restClient;

    public DiscordService(AppProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    public String createDm(String userId) {
        Map<?, ?> response = restClient.post()
                .uri("https://discord.com/api/v10/users/@me/channels")
                .header(HttpHeaders.AUTHORIZATION, "Bot " + properties.discord().botToken())
                .body(Map.of("recipient_id", userId))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("id") == null) {
            throw new IllegalStateException("Discord did not return a DM channel id");
        }
        return response.get("id").toString();
    }

    public void sendEmbed(String channelId, String title, String description, int color, Map<String, Object> fields) {
        List<Map<String, Object>> embedFields = fields.entrySet().stream()
                .map(entry -> Map.<String, Object>of(
                        "name", entry.getKey(),
                        "value", String.valueOf(entry.getValue()),
                        "inline", true
                ))
                .toList();

        Map<String, Object> embed = Map.of(
                "title", title,
                "description", description,
                "color", color,
                "timestamp", OffsetDateTime.now().toString(),
                "fields", embedFields
        );

        restClient.post()
                .uri("https://discord.com/api/v10/channels/{channelId}/messages", channelId)
                .header(HttpHeaders.AUTHORIZATION, "Bot " + properties.discord().botToken())
                .body(Map.of("embeds", List.of(embed)))
                .retrieve()
                .toBodilessEntity();
    }
}
