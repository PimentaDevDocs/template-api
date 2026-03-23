package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.domain.User;
import com.pimentadesenvolvimento.dto.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public UserEventPublisher(RabbitTemplate rabbitTemplate,
                              @org.springframework.beans.factory.annotation.Value("${app.rabbitmq.exchange}") String exchange,
                              @org.springframework.beans.factory.annotation.Value("${app.rabbitmq.routing-key.user-created}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishUserCreated(User user) {
        if (user == null) {
            return;
        }
        UserCreatedEvent event = new UserCreatedEvent(user.getId(), user.getUsername(), Instant.now());
        log.info("Publishing user creation event to rabbitmq: {}", event);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
