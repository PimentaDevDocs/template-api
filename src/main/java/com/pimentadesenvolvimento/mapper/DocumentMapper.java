package com.pimentadesenvolvimento.mapper;

import com.pimentadesenvolvimento.domain.Document;
import com.pimentadesenvolvimento.dto.DocumentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(source = "id", target = "documentId")
    DocumentDTO toDto(Document entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "person", ignore = true)
    Document toEntity(DocumentDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "person", ignore = true)
    void updateEntityFromDto(DocumentDTO dto, @MappingTarget Document entity);
}
