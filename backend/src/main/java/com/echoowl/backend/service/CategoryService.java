package com.echoowl.backend.service;

import com.echoowl.backend.model.Event;
import com.echoowl.backend.model.EventCategory;
import com.echoowl.backend.model.User;
import com.echoowl.backend.repository.EventCategoryRepository;
import com.echoowl.backend.repository.EventRepository;
import com.echoowl.backend.repository.UserRepository;
import com.echoowl.backend.util.Validation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CategoryService {
    private final EventCategoryRepository categories;
    private final EventRepository events;
    private final UserRepository users;

    public CategoryService(EventCategoryRepository categories, EventRepository events, UserRepository users) {
        this.categories = categories;
        this.events = events;
        this.users = users;
    }

    public Map<String, Object> getEventCategories(User user) {
        OffsetDateTime firstDayOfMonth = OffsetDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();

        List<Map<String, Object>> payload = categories.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .map(category -> categorySummary(category, firstDayOfMonth))
                .toList();

        return Map.of("categories", payload);
    }

    private Map<String, Object> categorySummary(EventCategory category, OffsetDateTime firstDayOfMonth) {
        List<Event> monthlyEvents = events.findByEventCategoryIdAndCreatedAtGreaterThanEqual(category.getId(), firstDayOfMonth);
        Set<String> fieldNames = new HashSet<>();
        OffsetDateTime lastPing = monthlyEvents.stream()
                .peek(event -> fieldNames.addAll(event.getFields().keySet()))
                .map(Event::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", category.getId());
        summary.put("name", category.getName());
        summary.put("emoji", category.getEmoji());
        summary.put("color", category.getColor());
        summary.put("updatedAt", category.getUpdatedAt());
        summary.put("createdAt", category.getCreatedAt());
        summary.put("uniqueFieldCount", fieldNames.size());
        summary.put("eventsCount", monthlyEvents.size());
        summary.put("lastPing", lastPing);
        return summary;
    }

    @Transactional
    public Map<String, Object> createEventCategory(User user, String name, String color, String emoji) {
        Validation.categoryName(name);
        String normalizedName = name.toLowerCase();
        if (categories.existsByNameAndUserId(normalizedName, user.getId())) {
            throw new IllegalArgumentException("Category \"" + normalizedName + "\" already exists");
        }

        User managedUser = users.getReferenceById(user.getId());

        EventCategory category = new EventCategory();
        category.setName(normalizedName);
        category.setColor(Validation.hexColorToInt(color));
        category.setEmoji(emoji);
        category.setUser(managedUser);

        return Map.of("eventCategory", categoryPayload(categories.save(category)));
    }

    @Transactional
    public Map<String, Object> deleteCategory(User user, String name) {
        categories.deleteByNameAndUserId(name, user.getId());
        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> insertQuickstartCategories(User user) {
        int created = 0;
        created += createIfMissing(user, "bug", "🐛", 0xff6b6b);
        created += createIfMissing(user, "sale", "💰", 0xffeb3b);
        created += createIfMissing(user, "question", "🤔", 0x6c5ce7);
        return Map.of("success", true, "count", created);
    }

    private int createIfMissing(User user, String name, String emoji, int color) {
        if (categories.existsByNameAndUserId(name, user.getId())) {
            return 0;
        }
        EventCategory category = new EventCategory();
        category.setUser(users.getReferenceById(user.getId()));
        category.setName(name);
        category.setEmoji(emoji);
        category.setColor(color);
        categories.save(category);
        return 1;
    }

    public Map<String, Object> pollCategory(User user, String name) {
        Validation.categoryName(name);
        EventCategory category = categories.findByNameAndUserId(name, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category \"" + name + "\" not found"));

        return Map.of("hasEvents", events.countForCategory(category.getId()) > 0);
    }

    public Map<String, Object> getEventsByCategoryName(User user, String name, int page, int limit, String timeRange) {
        Validation.categoryName(name);
        if (page < 1 || limit < 1 || limit > 50) {
            throw new IllegalArgumentException("Invalid pagination");
        }

        OffsetDateTime startDate = startDate(timeRange);
        var pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = events.findByEventCategoryNameAndEventCategoryUserIdAndCreatedAtGreaterThanEqual(
                name,
                user.getId(),
                startDate,
                pageable
        );

        long count = events.countByEventCategoryNameAndEventCategoryUserIdAndCreatedAtGreaterThanEqual(name, user.getId(), startDate);
        List<Map<String, Object>> payload = result.getContent().stream().map(this::eventPayload).toList();
        return Map.of("events", payload, "eventsCount", count);
    }

    private Map<String, Object> eventPayload(Event event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", event.getId());
        payload.put("formattedMessage", event.getFormattedMessage());
        payload.put("userId", event.getUser().getId());
        payload.put("name", event.getName());
        payload.put("fields", event.getFields());
        payload.put("deliveryStatus", event.getDeliveryStatus());
        payload.put("createdAt", event.getCreatedAt());
        payload.put("updatedAt", event.getUpdatedAt());
        payload.put("eventCategoryId", event.getEventCategory() == null ? null : event.getEventCategory().getId());
        return payload;
    }

    private Map<String, Object> categoryPayload(EventCategory category) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", category.getId());
        payload.put("name", category.getName());
        payload.put("color", category.getColor());
        payload.put("emoji", category.getEmoji());
        payload.put("userId", category.getUser().getId());
        payload.put("createdAt", category.getCreatedAt());
        payload.put("updatedAt", category.getUpdatedAt());
        return payload;
    }

    private OffsetDateTime startDate(String timeRange) {
        OffsetDateTime now = OffsetDateTime.now();
        return switch (timeRange) {
            case "today" -> now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
            case "week" -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                    .toLocalDate()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toOffsetDateTime();
            case "month" -> now.withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
            default -> throw new IllegalArgumentException("Invalid timeRange");
        };
    }
}
