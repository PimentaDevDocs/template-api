package com.pimentadesenvolvimento.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserGrowthSnapshotDTO(
        LocalDate snapshotDate,
        Long userCount,
        LocalDateTime createdAt
) {
}
