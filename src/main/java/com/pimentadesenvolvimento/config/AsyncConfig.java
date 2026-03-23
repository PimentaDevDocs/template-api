package com.pimentadesenvolvimento.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @Bean(name = "auditTaskExecutor")
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("audit-log-");

        RejectedExecutionHandler rejectionHandler = (r, e) -> {
            log.error("Audit log task rejected — executor queue is full. Current queue size: {}",
                    ((ThreadPoolExecutor) e).getQueue().size());
        };
        executor.setRejectedExecutionHandler(rejectionHandler);

        executor.initialize();
        return executor;
    }
}
