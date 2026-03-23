package com.pimentadesenvolvimento.conroledebolsaoback.repository;

import com.pimentadesenvolvimento.conroledebolsaoback.domain.UserGrowthSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserGrowthSnapshotRepository extends JpaRepository<UserGrowthSnapshot, Long> {
    Optional<UserGrowthSnapshot> findFirstByOrderBySnapshotDateDesc();

    Optional<UserGrowthSnapshot> findBySnapshotDate(LocalDate date);
}
