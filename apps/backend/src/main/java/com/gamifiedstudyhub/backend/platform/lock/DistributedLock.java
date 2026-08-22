package com.gamifiedstudyhub.backend.platform.lock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Small helper for "run this critical section under a cluster-wide lock", used by
 * anti-double-spend / idempotent flows. Present only when a {@link RedissonClient}
 * bean exists (i.e. {@code app.redisson.enabled=true}).
 */
@Component
@ConditionalOnBean(RedissonClient.class)
public class DistributedLock {

    private final RedissonClient redisson;

    public DistributedLock(RedissonClient redisson) {
        this.redisson = redisson;
    }

    /**
     * Acquire {@code lock:<key>}, run {@code action}, then release. The lock auto-expires
     * after {@code lease} even if the holder dies, and acquisition gives up after {@code wait}.
     *
     * @throws IllegalStateException if the lock cannot be acquired within {@code wait}
     */
    public <T> T runLocked(String key, Duration wait, Duration lease, Supplier<T> action) {
        RLock lock = redisson.getLock("lock:" + key);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(wait.toMillis(), lease.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new IllegalStateException("Could not acquire lock: " + key);
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while acquiring lock: " + key, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
