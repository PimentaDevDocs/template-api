package com.pimentadesenvolvimento.conroledebolsaoback.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@Slf4j
public class WebhookAuthFilter extends OncePerRequestFilter {
    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String WEBHOOK_PATH = "/webhook";

    @Value("${webhook.secret:}")
    private String webhookSecret;

    @PostConstruct
    void resolveSecret() {
        if (webhookSecret == null || webhookSecret.isBlank() || webhookSecret.equals("${WEBHOOK_SECRET}")) {
            String envSecret = System.getenv("WEBHOOK_SECRET");
            if (envSecret != null && !envSecret.isBlank()) {
                this.webhookSecret = envSecret;
                log.info("Webhook secret resolved from environment variable");
            } else {
                webhookSecret = null;
                log.warn("Webhook secret not configured. Webhook endpoints will be unavailable. Set WEBHOOK_SECRET environment variable to enable.");
            }
        }
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(WEBHOOK_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if webhook secret is configured
        if (webhookSecret == null || webhookSecret.isBlank()) {
            sendError(response, HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "Webhook service is not configured. Contact administrator.");
            return;
        }

        ReadableBodyRequestWrapper wrappedRequest = new ReadableBodyRequestWrapper(request);
        String signatureHeader = wrappedRequest.getHeader(SIGNATURE_HEADER);
        if (signatureHeader == null) {
            sendError(response, HttpStatus.UNAUTHORIZED.value(), Messages.Security.WEBHOOK_SIGNATURE_MISSING);
            return;
        }
        try {
            byte[] payload = wrappedRequest.getBody();
            String calculatedSignature = calculateHmacSha256(payload, webhookSecret);

            byte[] expected = calculatedSignature.getBytes(StandardCharsets.UTF_8);
            byte[] provided = signatureHeader.getBytes(StandardCharsets.UTF_8);

            if (MessageDigest.isEqual(expected, provided)) {
                // Signature valid - check timestamp for replay attack prevention
                String timestampHeader = wrappedRequest.getHeader("X-Timestamp");
                if (timestampHeader != null && !timestampHeader.isBlank()) {
                    try {
                        long timestamp = Long.parseLong(timestampHeader);
                        long now = java.time.Instant.now().getEpochSecond();
                        long diff = Math.abs(now - timestamp);
                        if (diff > 300) {
                            sendError(response, HttpStatus.UNAUTHORIZED.value(),
                                    "Webhook timestamp expired or too far in the future");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid timestamp header format: {}", timestampHeader);
                    }
                } else {
                    log.warn("X-Timestamp header missing. Replay protection is inactive.");
                }
                filterChain.doFilter(wrappedRequest, response);
            } else {
                sendError(response, HttpStatus.FORBIDDEN.value(), Messages.Security.WEBHOOK_SIGNATURE_INVALID);
            }
        } catch (Exception e) {
            log.error("Error validating webhook signature", e);
            sendError(response, HttpStatus.INTERNAL_SERVER_ERROR.value(), Messages.Security.WEBHOOK_SIGNATURE_INTERNAL_ERROR);
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    private String calculateHmacSha256(byte[] payload, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] hmacSha256 = mac.doFinal(payload);
        return Base64.getEncoder().encodeToString(hmacSha256);
    }
}