package com.echoowl.backend.controller;

import com.echoowl.backend.service.PaymentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/stripe")
public class StripeWebhookController {
    private final PaymentService payments;

    public StripeWebhookController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping
    public ResponseEntity<String> handle(@RequestBody String body, @RequestHeader HttpHeaders headers) throws Exception {
        payments.handleWebhook(body, headers.getFirst("stripe-signature"));
        return ResponseEntity.ok("OK");
    }
}
