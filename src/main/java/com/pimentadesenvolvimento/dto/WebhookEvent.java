package com.pimentadesenvolvimento.dto;

import java.time.Instant;
import java.util.Map;

public record WebhookEvent(
        String event,
        Map<String, Object> payload,
        Instant receivedAt
) {
}
