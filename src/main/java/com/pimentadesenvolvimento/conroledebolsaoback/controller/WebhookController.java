package com.pimentadesenvolvimento.conroledebolsaoback.controller;

import com.pimentadesenvolvimento.conroledebolsaoback.service.WebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<Void> onWebhook(
            @RequestHeader(value = "X-Event", required = false) String event,
            @RequestBody Map<String, Object> payload
    ) {
        webhookService.handleWebhook(event, payload);
        return ResponseEntity.ok().build();
    }
}
