package com.pimentadesenvolvimento.conroledebolsaoback.mapper;

import com.pimentadesenvolvimento.conroledebolsaoback.domain.UserGrowthSnapshot;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.UserGrowthSnapshotDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserGrowthSnapshotMapper {
    UserGrowthSnapshotDTO toDTO(UserGrowthSnapshot snapshot);
}
