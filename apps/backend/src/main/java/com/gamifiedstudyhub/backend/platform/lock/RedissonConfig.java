package com.gamifiedstudyhub.backend.platform.lock;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson client for distributed locks + atomic ops (e.g. coin-ledger idempotency).
 * OFF by default; the client — which dials Redis eagerly — is created only when
 * {@code app.redisson.enabled=true}. Reuses the same Redis coordinates as Spring Data.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.redisson", name = "enabled", havingValue = "true")
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${spring.data.redis.password:}") String password
    ) {
        Config config = new Config();
        SingleServerConfig server = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port);
        if (password != null && !password.isBlank()) {
            server.setPassword(password);
        }
        return Redisson.create(config);
    }
}
