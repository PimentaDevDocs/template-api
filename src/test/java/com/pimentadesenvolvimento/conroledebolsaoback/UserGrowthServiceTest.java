package com.pimentadesenvolvimento.conroledebolsaoback;

import com.pimentadesenvolvimento.conroledebolsaoback.domain.User;
import com.pimentadesenvolvimento.conroledebolsaoback.domain.UserGrowthSnapshot;
import com.pimentadesenvolvimento.conroledebolsaoback.repository.UserGrowthSnapshotRepository;
import com.pimentadesenvolvimento.conroledebolsaoback.repository.UserRepository;
import com.pimentadesenvolvimento.conroledebolsaoback.service.UserGrowthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({UserGrowthService.class, UserGrowthServiceTest.TestAuditConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserGrowthServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserGrowthSnapshotRepository snapshotRepository;
    @Autowired
    private UserGrowthService growthService;

    @Test
    void captureDailySnapshot_createsOrUpdatesSnapshot() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password123!");
        user1.setEmail("user1@example.com");
        user1.setName("User One");
        user1.setRoles(new HashSet<>());
        userRepository.save(user1);

        UserGrowthSnapshot yesterday = new UserGrowthSnapshot(LocalDate.now().minusDays(1), 1L);
        snapshotRepository.save(yesterday);

        growthService.captureDailySnapshot();

        LocalDate today = LocalDate.now();
        UserGrowthSnapshot snapshot = snapshotRepository.findBySnapshotDate(today)
                .orElseThrow();

        assertThat(snapshot.getUserCount()).isEqualTo(userRepository.count());
        assertThat(snapshotRepository.findAll()).hasSize(2);
    }

    @Configuration
    @EnableJpaAuditing
    static class TestAuditConfig {
        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-user");
        }
    }
}
