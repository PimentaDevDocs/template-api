package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.dto.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.user-created}")
    public void onUserCreated(UserCreatedEvent event) {
        log.info("Received user-created event: {}", event);
        // Extend this method to react to user creation events (send emails, analytics, etc.)
    }
}
