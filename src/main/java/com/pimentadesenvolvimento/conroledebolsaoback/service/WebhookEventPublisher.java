package com.pimentadesenvolvimento.conroledebolsaoback.service;

import com.pimentadesenvolvimento.conroledebolsaoback.dto.WebhookEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
@Slf4j
public class WebhookEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public WebhookEventPublisher(
            RabbitTemplate rabbitTemplate,
            @org.springframework.beans.factory.annotation.Value("${app.rabbitmq.exchange}") String exchange,
            @org.springframework.beans.factory.annotation.Value("${app.rabbitmq.routing-key.webhook}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(WebhookEvent event) {
        if (event == null) {
            return;
        }
        log.debug("Publishing webhook event to rabbitmq: {}", event);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
