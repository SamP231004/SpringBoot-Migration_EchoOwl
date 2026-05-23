package com.echoowl.backend.controller;

import com.echoowl.backend.model.User;
import com.echoowl.backend.service.AuthService;
import com.echoowl.backend.service.PaymentService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final AuthService auth;
    private final PaymentService payments;

    public PaymentController(AuthService auth, PaymentService payments) {
        this.auth = auth;
        this.payments = payments;
    }

    @PostMapping("/createCheckoutSession")
    public Map<String, Object> createCheckoutSession(@RequestHeader HttpHeaders headers) throws Exception {
        User user = auth.requireUser(headers);
        return Map.of("url", payments.createCheckoutSession(user));
    }

    @GetMapping("/getUserPlan")
    public Map<String, Object> getUserPlan(@RequestHeader HttpHeaders headers) {
        User user = auth.requireUser(headers);
        return Map.of("plan", user.getPlan().name());
    }
}
