package com.pimentadesenvolvimento.mapper;

import com.pimentadesenvolvimento.domain.Person;
import com.pimentadesenvolvimento.dto.PersonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {DocumentMapper.class, ContactMapper.class})
public interface PersonMapper {

    @Mapping(source = "id", target = "personId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "contacts", target = "contacts")
    PersonDTO toDto(Person entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    Person toEntity(PersonDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    void updateEntityFromDto(PersonDTO dto, @MappingTarget Person entity);
}
