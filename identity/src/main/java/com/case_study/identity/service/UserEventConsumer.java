package com.case_study.identity.service;

import com.case_study.identity.dto.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventConsumer {


    @JmsListener(destination = "${app.jms.notification-queue}")
    public void handleSendWelcomingEmail(UserRegisteredEvent event) {
        log.info("Received registration event for userId={}, email={}", event.getUserId(), event.getEmail());

        simulateNotificationProcessing(event);

        log.info("Welcome email simulated as sent to {} (userId={})", event.getEmail(), event.getUserId());
    }


    private void simulateNotificationProcessing(UserRegisteredEvent event) {
        try {
            // Simulates latency of an external service (e.g. an email provider)
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Notification processing interrupted for userId={}", event.getUserId());
        }
    }
}
