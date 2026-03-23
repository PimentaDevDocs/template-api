package com.pimentadesenvolvimento.dto;

import java.time.Instant;

/**
 * Simple message sent to RabbitMQ when a new user is created.
 */
public record UserCreatedEvent(Long userId, String username, Instant createdAt) {
}
