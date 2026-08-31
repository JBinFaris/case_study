package com.case_study.identity.service;

import com.case_study.identity.dto.UserRegisteredEvent;
import jakarta.jms.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventProducer {

    private final JmsTemplate jmsTemplate;
    private final Queue userRegistrationQueue;

    public void publish(UserRegisteredEvent event) {
        log.debug("Publishing UserRegisteredEvent for userId={}", event.getUserId());
        jmsTemplate.convertAndSend(userRegistrationQueue, event);
        log.info("Published registration event to queue for userId={}", event.getUserId());
    }

}
