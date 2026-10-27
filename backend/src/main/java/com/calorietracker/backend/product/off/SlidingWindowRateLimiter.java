package com.calorietracker.backend.product.off;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public class SlidingWindowRateLimiter {

    private final int maxRequests;
    private final Duration window;
    private final Deque<Instant> timestamps = new ArrayDeque<>();

    public SlidingWindowRateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }

    public synchronized boolean tryAcquire() {
        Instant now = Instant.now();
        Instant threshold = now.minus(window);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(threshold)) {
            timestamps.pollFirst();
        }
        if (timestamps.size() >= maxRequests) {
            return false;
        }
        timestamps.addLast(now);
        return true;
    }
}