package com.echoowl.backend.service;

import com.echoowl.backend.model.DeliveryStatus;
import com.echoowl.backend.model.Event;
import com.echoowl.backend.model.EventCategory;
import com.echoowl.backend.model.Quota;
import com.echoowl.backend.model.User;
import com.echoowl.backend.repository.EventCategoryRepository;
import com.echoowl.backend.repository.EventRepository;
import com.echoowl.backend.repository.QuotaRepository;
import com.echoowl.backend.repository.UserRepository;
import com.echoowl.backend.util.Validation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class EventIngestionService {
    private final UserRepository users;
    private final EventCategoryRepository categories;
    private final EventRepository events;
    private final QuotaRepository quotas;
    private final DiscordService discord;

    public EventIngestionService(
            UserRepository users,
            EventCategoryRepository categories,
            EventRepository events,
            QuotaRepository quotas,
            DiscordService discord
    ) {
        this.users = users;
        this.categories = categories;
        this.events = events;
        this.quotas = quotas;
        this.discord = discord;
    }

    @Transactional
    public Map<String, Object> ingest(String apiKey, String categoryName, Map<String, Object> fields, String description) {
        Validation.categoryName(categoryName);

        User user = users.findByApiKey(apiKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key"));

        if (user.getDiscordId() == null || user.getDiscordId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please enter your discord ID in your account settings");
        }

        OffsetDateTime now = OffsetDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        Quota quota = quotas.findByUserId(user.getId()).orElse(null);
        boolean quotaIsCurrentMonth = quota != null && quota.getYear() == currentYear && quota.getMonth() == currentMonth;
        int quotaLimit = QuotaLimits.eventsFor(user.getPlan());
        if (quotaIsCurrentMonth && quota.getCount() >= quotaLimit) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Monthly quota reached. Please upgrade your plan for more events");
        }

        EventCategory category = categories.findByNameAndUserId(categoryName, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "You dont have a category named \"" + categoryName + "\""));

        String title = (category.getEmoji() == null ? "🔔" : category.getEmoji()) + " " + capitalize(category.getName());
        String effectiveDescription = description == null || description.isBlank()
                ? "A new " + category.getName() + " event has occurred!"
                : description;

        Event event = new Event();
        event.setName(category.getName());
        event.setFormattedMessage(title + "\n\n" + effectiveDescription);
        event.setUser(user);
        event.setFields(fields);
        event.setEventCategory(category);
        event = events.save(event);

        try {
            String channelId = discord.createDm(user.getDiscordId());
            discord.sendEmbed(channelId, title, effectiveDescription, category.getColor(), fields);
            event.setDeliveryStatus(DeliveryStatus.DELIVERED);

            Quota existing = quota == null ? new Quota() : quota;
            existing.setUser(user);
            existing.setYear(currentYear);
            existing.setMonth(currentMonth);
            existing.setCount(quotaIsCurrentMonth ? existing.getCount() + 1 : 1);
            quotas.save(existing);
        } catch (Exception ex) {
            event.setDeliveryStatus(DeliveryStatus.FAILED);
            events.save(event);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing event");
        }

        events.save(event);
        return Map.of("message", "Event processed successfully", "eventId", event.getId());
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
