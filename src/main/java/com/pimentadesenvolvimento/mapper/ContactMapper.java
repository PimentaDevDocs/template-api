package com.pimentadesenvolvimento.mapper;

import com.pimentadesenvolvimento.domain.Contact;
import com.pimentadesenvolvimento.dto.ContactDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContactMapper {
    @Mapping(source = "id", target = "contactId")
    @Mapping(source = "type.id", target = "contactTypeId")
    @Mapping(source = "type.name", target = "contactTypeName")
    ContactDTO toDto(Contact entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "type", ignore = true)
    Contact toEntity(ContactDTO dto);
}