package com.pimentadesenvolvimento.conroledebolsaoback.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserGrowthScheduler {

    private final UserGrowthService growthService;

    public UserGrowthScheduler(UserGrowthService growthService) {
        this.growthService = growthService;
    }

    /**
     * Runs once a day at midnight to capture user base growth.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void captureDailyGrowth() {
        growthService.captureDailySnapshot();
    }
}
