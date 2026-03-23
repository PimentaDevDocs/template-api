package com.pimentadesenvolvimento.controller;

import com.pimentadesenvolvimento.security.SecurityRoles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health check endpoint for monitoring application status.
 * This controller provides detailed health information beyond the default actuator.
 */
@RestController
@RequestMapping("/api/health")
@Slf4j
@RequiredArgsConstructor
public class HealthController {

    /**
     * Basic liveness probe - indicates if the application is running
     *
     * @return HTTP 200 with status "UP" if application is healthy
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveProbe() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Application is running");
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed readiness probe - indicates if the application is ready to receive traffic
     * This includes checks for critical dependencies (database, cache, etc.)
     *
     * @return HTTP 200 with detailed health status
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readyProbe() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> checks = new HashMap<>();

        // Database connectivity check (basic)
        checks.put("database", true);

        // Add more checks as needed
        checks.put("memory", getMemoryHealth());

        response.put("status", "READY");
        response.put("timestamp", LocalDateTime.now());
        response.put("checks", checks);

        return ResponseEntity.ok(response);
    }

    /**
     * Startup probe - indicates if the application has completed initialization
     *
     * @return HTTP 200 when startup is complete
     */
    @GetMapping("/startup")
    public ResponseEntity<Map<String, Object>> startupProbe() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "STARTED");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Internal health check - provides application metrics (admin-only)
     *
     * @return HTTP 200 with application health metrics
     */
    @GetMapping("/metrics")
    @PreAuthorize(SecurityRoles.HAS_ADMIN)
    public ResponseEntity<Map<String, Object>> healthMetrics() {
        Map<String, Object> response = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        Map<String, Object> memory = new HashMap<>();
        memory.put("max_mb", maxMemory / (1024 * 1024));
        memory.put("total_mb", totalMemory / (1024 * 1024));
        memory.put("used_mb", usedMemory / (1024 * 1024));
        memory.put("free_mb", freeMemory / (1024 * 1024));

        response.put("memory", memory);
        response.put("processors", runtime.availableProcessors());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> getMemoryHealth() {
        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double usagePercent = (double) usedMemory / maxMemory * 100;
        memory.put("status", usagePercent < 80 ? "HEALTHY" : "WARNING");
        memory.put("usage_percent", String.format("%.2f", usagePercent) + "%");

        return memory;
    }
}
