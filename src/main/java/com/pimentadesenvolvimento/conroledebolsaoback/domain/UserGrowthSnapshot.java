package com.pimentadesenvolvimento.conroledebolsaoback.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "user_growth_snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
/*
 * Lombok @AllArgsConstructor generates a constructor for (snapshotDate, userCount) ONLY.
 * BaseEntity fields (id, createdAt, etc.) are NOT included — that is intentional.
 * If you add new fields to this class, update all direct constructor call sites:
 * UserGrowthService.captureDailySnapshot() and UserGrowthServiceTest.
 */
public class UserGrowthSnapshot extends BaseEntity {

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false)
    private Long userCount;
}
