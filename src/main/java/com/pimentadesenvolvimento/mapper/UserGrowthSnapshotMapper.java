package com.pimentadesenvolvimento.mapper;

import com.pimentadesenvolvimento.domain.UserGrowthSnapshot;
import com.pimentadesenvolvimento.dto.UserGrowthSnapshotDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserGrowthSnapshotMapper {
    UserGrowthSnapshotDTO toDTO(UserGrowthSnapshot snapshot);
}
