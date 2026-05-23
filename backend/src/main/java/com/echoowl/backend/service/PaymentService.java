package com.echoowl.backend.service;

import com.echoowl.backend.config.AppProperties;
import com.echoowl.backend.model.Plan;
import com.echoowl.backend.model.User;
import com.echoowl.backend.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final AppProperties properties;
    private final UserRepository users;

    public PaymentService(AppProperties properties, UserRepository users) {
        this.properties = properties;
        this.users = users;
    }

    public String createCheckoutSession(User user) throws Exception {
        Stripe.apiKey = properties.stripe().secretKey();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(properties.appUrl() + "/dashboard?success=true")
                .setCancelUrl(properties.appUrl() + "/pricing")
                .setCustomerEmail(user.getEmail())
                .putMetadata("userId", user.getId())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(properties.stripe().priceId())
                        .setQuantity(1L)
                        .build())
                .build();

        return Session.create(params).getUrl();
    }

    @Transactional
    public void handleWebhook(String body, String signature) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(body, signature, properties.stripe().webhookSecret().trim());

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Stripe payload"));

            String userId = session.getMetadata().get("userId");
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("Invalid metadata");
            }

            User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setPlan(Plan.PRO);
            users.save(user);
        }
    }
}
