package com.pimentadesenvolvimento.conroledebolsaoback.controller;

import com.pimentadesenvolvimento.conroledebolsaoback.dto.AuditLogDTO;
import com.pimentadesenvolvimento.conroledebolsaoback.mapper.AuditLogMapper;
import com.pimentadesenvolvimento.conroledebolsaoback.security.SecurityRoles;
import com.pimentadesenvolvimento.conroledebolsaoback.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogMapper auditLogMapper;

    public AuditLogController(AuditLogService auditLogService, AuditLogMapper auditLogMapper) {
        this.auditLogService = auditLogService;
        this.auditLogMapper = auditLogMapper;
    }

    @GetMapping
    @PreAuthorize(SecurityRoles.HAS_ADMIN)
    public Page<AuditLogDTO> listAuditLogs(Pageable pageable) {
        return auditLogService.findAll(pageable)
                .map(auditLogMapper::toDTO);
    }
}
