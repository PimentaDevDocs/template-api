package com.pimentadesenvolvimento.conroledebolsaoback.mapper;

import com.pimentadesenvolvimento.conroledebolsaoback.domain.AuditLogEntry;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.AuditLogDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogDTO toDTO(AuditLogEntry entity);
}
