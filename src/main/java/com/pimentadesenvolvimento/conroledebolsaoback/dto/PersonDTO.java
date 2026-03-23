package com.pimentadesenvolvimento.conroledebolsaoback.dto;

import java.time.LocalDate;
import java.util.Set;

public record PersonDTO(
        Long personId,
        Long userId,
        String name,
        LocalDate birthDate,
        Set<DocumentDTO> documents,
        Set<ContactDTO> contacts
) {
}
