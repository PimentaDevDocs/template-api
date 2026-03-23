package com.pimentadesenvolvimento.mapper;

import com.pimentadesenvolvimento.domain.AuditLogEntry;
import com.pimentadesenvolvimento.dto.AuditLogDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogDTO toDTO(AuditLogEntry entity);
}
