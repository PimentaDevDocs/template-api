package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.dto.WebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final ObjectProvider<WebhookEventPublisher> publisher;
    private final AuditLogService auditLogService;

    public void handleWebhook(String event, Map<String, Object> payload) {
        log.info("Received webhook event [{}] with payload: {}", event, payload);

        auditLogService.log("WEBHOOK_RECEIVED", null, "event=" + event + " payload=" + payload);

        publisher.ifAvailable(p -> p.publish(new WebhookEvent(
                event,
                payload,
                Instant.now()
        )));
    }
}
