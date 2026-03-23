package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.domain.AuditLogEntry;
import com.pimentadesenvolvimento.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async("auditTaskExecutor")
    public void log(String action, String principal, String details) {
        AuditLogEntry entry = new AuditLogEntry(action, principal, details);
        auditLogRepository.save(entry);
    }

    public Page<AuditLogEntry> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}
