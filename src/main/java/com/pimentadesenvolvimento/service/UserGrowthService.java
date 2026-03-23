package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.domain.UserGrowthSnapshot;
import com.pimentadesenvolvimento.repository.UserGrowthSnapshotRepository;
import com.pimentadesenvolvimento.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserGrowthService {

    private static final Logger log = LoggerFactory.getLogger(UserGrowthService.class);

    private final UserRepository userRepository;
    private final UserGrowthSnapshotRepository snapshotRepository;

    public UserGrowthService(UserRepository userRepository, UserGrowthSnapshotRepository snapshotRepository) {
        this.userRepository = userRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Transactional
    public UserGrowthSnapshot captureDailySnapshot() {
        LocalDate today = LocalDate.now();
        long userCount = userRepository.count();

        Optional<UserGrowthSnapshot> previous = snapshotRepository.findFirstByOrderBySnapshotDateDesc();
        long previousCount = previous.map(UserGrowthSnapshot::getUserCount).orElse(0L);
        long delta = userCount - previousCount;

        log.info("Daily user snapshot recorded ({}): total={}, delta={}", today, userCount, delta);

        UserGrowthSnapshot snapshot = snapshotRepository.findBySnapshotDate(today)
                .orElseGet(() -> new UserGrowthSnapshot(today, userCount));
        snapshot.setUserCount(userCount);
        return snapshotRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public List<UserGrowthSnapshot> getAllSnapshots() {
        return snapshotRepository.findAll();
    }
}
