package com.croi.common.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, per-IP token bucket rate limiter.
 *
 * Known limitation: {@code cache} never evicts entries, so it grows for as long
 * as the app runs and new IPs show up — including under the exact sustained/
 * scanning traffic this is meant to defend against. Fine for a single-instance,
 * low-volume endpoint like the contact form; if this needs to scale to more
 * endpoints or survive long-running abuse, replace with a time-bounded cache
 * (e.g. Caffeine with expireAfterAccess) or move state to Redis for multi-instance
 * deployments (this in-memory map won't share limits across app instances).
 */
@Component
public class RateLimiter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientIp) {
        Bucket bucket = cache.computeIfAbsent(clientIp, ip -> {
            Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofHours(1)));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });
        return bucket.tryConsume(1);
    }

    /**
     * Same in-memory bucket cache, generalized to an arbitrary key and rate — e.g. per-workspace
     * limits on a webhook. Callers must namespace their keys (e.g. "voice:{workspaceId}") so they
     * can't collide with plain client-IP keys used by {@link #allowRequest(String)}.
     */
    public boolean allowRequest(String key, int capacity, Duration period) {
        Bucket bucket = cache.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, period));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });
        return bucket.tryConsume(1);
    }
}
