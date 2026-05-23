package com.echoowl.backend.controller;

import com.echoowl.backend.model.User;
import com.echoowl.backend.repository.EventCategoryRepository;
import com.echoowl.backend.repository.QuotaRepository;
import com.echoowl.backend.repository.UserRepository;
import com.echoowl.backend.service.AuthService;
import com.echoowl.backend.service.QuotaLimits;
import com.echoowl.backend.util.InputParser;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
public class ProjectController {
    private final AuthService auth;
    private final UserRepository users;
    private final QuotaRepository quotas;
    private final EventCategoryRepository categories;
    private final InputParser inputParser;

    public ProjectController(
            AuthService auth,
            UserRepository users,
            QuotaRepository quotas,
            EventCategoryRepository categories,
            InputParser inputParser
    ) {
        this.auth = auth;
        this.users = users;
        this.quotas = quotas;
        this.categories = categories;
        this.inputParser = inputParser;
    }

    @GetMapping("/getUsage")
    public Map<String, Object> getUsage(@RequestHeader HttpHeaders headers) {
        User user = auth.requireUser(headers);
        OffsetDateTime currentMonth = OffsetDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        int eventsUsed = quotas.findByUserIdAndYearAndMonth(user.getId(), currentMonth.getYear(), currentMonth.getMonthValue())
                .map(quota -> quota.getCount())
                .orElse(0);

        return Map.of(
                "categoriesUsed", categories.countByUserId(user.getId()),
                "categoriesLimit", QuotaLimits.categoriesFor(user.getPlan()),
                "eventsUsed", eventsUsed,
                "eventsLimit", QuotaLimits.eventsFor(user.getPlan()),
                "resetDate", currentMonth.plusMonths(1)
        );
    }

    @PostMapping("/setDiscordID")
    @Transactional
    public Map<String, Object> setDiscordId(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> body) {
        User user = auth.requireUser(headers);
        Map<String, Object> input = inputParser.parseMap(body);
        String discordId = inputParser.requiredString(input, "discordId");
        if (discordId.length() > 20) {
            throw new IllegalArgumentException("discordId must be at most 20 characters");
        }

        user.setDiscordId(discordId);
        users.save(user);
        return Map.of("success", true);
    }
}
