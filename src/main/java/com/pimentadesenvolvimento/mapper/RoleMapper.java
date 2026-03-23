package com.pimentadesenvolvimento.mapper;

import com.pimentadesenvolvimento.domain.Role;
import com.pimentadesenvolvimento.dto.RoleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(source = "id", target = "roleId")
    RoleDTO toDto(Role entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Role toEntity(RoleDTO dto);
}