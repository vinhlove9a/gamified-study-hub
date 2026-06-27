package com.gamifiedstudyhub.backend.common.config;

import com.gamifiedstudyhub.backend.email.EmailProperties;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(EmailProperties.class)
public class AsyncConfig {

    /** Bounded executor for best-effort audit writes (drops to caller if saturated). */
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        return boundedExecutor("audit-", 2, 4, 500);
    }

    /** Bounded executor for outbound email (best-effort, must not block requests). */
    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        return boundedExecutor("mail-", 1, 3, 200);
    }

    private Executor boundedExecutor(String prefix, int core, int max, int queue) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queue);
        executor.setThreadNamePrefix(prefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
