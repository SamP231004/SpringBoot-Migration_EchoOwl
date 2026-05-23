package com.echoowl.backend.controller;

import com.echoowl.backend.model.User;
import com.echoowl.backend.service.AuthService;
import com.echoowl.backend.service.CategoryService;
import com.echoowl.backend.util.InputParser;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final AuthService auth;
    private final CategoryService categories;
    private final InputParser inputParser;

    public CategoryController(AuthService auth, CategoryService categories, InputParser inputParser) {
        this.auth = auth;
        this.categories = categories;
        this.inputParser = inputParser;
    }

    @GetMapping("/getEventCategories")
    public Map<String, Object> getEventCategories(@RequestHeader HttpHeaders headers) {
        return categories.getEventCategories(auth.requireUser(headers));
    }

    @PostMapping("/createEventCategory")
    public Map<String, Object> createEventCategory(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> body) {
        User user = auth.requireUser(headers);
        Map<String, Object> input = inputParser.parseMap(body);
        return categories.createEventCategory(
                user,
                inputParser.requiredString(input, "name"),
                inputParser.requiredString(input, "color"),
                input.get("emoji") == null ? null : input.get("emoji").toString()
        );
    }

    @PostMapping("/deleteCategory")
    public Map<String, Object> deleteCategory(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> body) {
        User user = auth.requireUser(headers);
        Map<String, Object> input = inputParser.parseMap(body);
        return categories.deleteCategory(user, inputParser.requiredString(input, "name"));
    }

    @PostMapping("/insertQuickstartCategories")
    public Map<String, Object> insertQuickstartCategories(@RequestHeader HttpHeaders headers) {
        return categories.insertQuickstartCategories(auth.requireUser(headers));
    }

    @GetMapping("/pollCategory")
    public Map<String, Object> pollCategory(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> query) {
        User user = auth.requireUser(headers);
        Map<String, Object> input = inputParser.parseMap(query);
        return categories.pollCategory(user, inputParser.requiredString(input, "name"));
    }

    @GetMapping("/getEventsByCategoryName")
    public Map<String, Object> getEventsByCategoryName(@RequestHeader HttpHeaders headers, @RequestParam Map<String, String> query) {
        User user = auth.requireUser(headers);
        Map<String, Object> input = inputParser.parseMap(query);
        return categories.getEventsByCategoryName(
                user,
                inputParser.requiredString(input, "name"),
                inputParser.requiredInt(input, "page"),
                inputParser.requiredInt(input, "limit"),
                inputParser.requiredString(input, "timeRange")
        );
    }
}
