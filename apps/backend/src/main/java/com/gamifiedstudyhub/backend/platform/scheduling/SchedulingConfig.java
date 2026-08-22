package com.gamifiedstudyhub.backend.platform.scheduling;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's {@code @Scheduled} together with ShedLock so a job runs on exactly
 * one instance at a time (safe when the app is scaled out). OFF by default — no
 * scheduler starts unless {@code app.scheduling.enabled=true}.
 *
 * <p>Annotate scheduled methods with
 * {@code @Scheduled(cron = "...")} + {@code @SchedulerLock(name = "...")}. Intended
 * for token cleanup, daily-reward resets, and triggering the DB backup script.
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
@ConditionalOnProperty(prefix = "app.scheduling", name = "enabled", havingValue = "true")
public class SchedulingConfig {

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory, "gsh");
    }
}
