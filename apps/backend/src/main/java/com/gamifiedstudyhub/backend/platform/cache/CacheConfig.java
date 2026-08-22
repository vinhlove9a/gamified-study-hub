package com.gamifiedstudyhub.backend.platform.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Spring Cache abstraction, OFF by default ({@code app.cache.enabled=false}) so
 * {@code @Cacheable} is inert until switched on. Two providers ship pre-wired:
 * in-process Caffeine (default, {@code app.cache.provider=caffeine}) and distributed
 * Redis ({@code app.cache.provider=redis}) for multi-instance correctness.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(prefix = "app.cache", name = "enabled", havingValue = "true")
public class CacheConfig {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    @Bean
    @ConditionalOnProperty(prefix = "app.cache", name = "provider", havingValue = "caffeine", matchIfMissing = true)
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(DEFAULT_TTL)
                .maximumSize(10_000));
        return manager;
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.cache", name = "provider", havingValue = "redis")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(DEFAULT_TTL))
                .build();
    }
}
