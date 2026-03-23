package com.pimentadesenvolvimento.controller;

import com.pimentadesenvolvimento.dto.UserGrowthSnapshotDTO;
import com.pimentadesenvolvimento.mapper.UserGrowthSnapshotMapper;
import com.pimentadesenvolvimento.security.SecurityRoles;
import com.pimentadesenvolvimento.service.UserGrowthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/growth")
public class UserGrowthController {

    private final UserGrowthService growthService;
    private final UserGrowthSnapshotMapper snapshotMapper;

    public UserGrowthController(UserGrowthService growthService, UserGrowthSnapshotMapper snapshotMapper) {
        this.growthService = growthService;
        this.snapshotMapper = snapshotMapper;
    }

    @GetMapping
    @PreAuthorize(SecurityRoles.HAS_ADMIN)
    public ResponseEntity<List<UserGrowthSnapshotDTO>> getGrowthHistory() {
        List<UserGrowthSnapshotDTO> snapshots = growthService.getAllSnapshots()
                .stream()
                .map(snapshotMapper::toDTO)
                .toList();
        return ResponseEntity.ok(snapshots);
    }
}
