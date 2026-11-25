package com.calorietracker.backend.product.off;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
class SlidingWindowRateLimiterTest {
    @Test void permitsRequestsUpToLimit() { var limiter=new SlidingWindowRateLimiter(2,Duration.ofDays(1)); assertTrue(limiter.tryAcquire()); assertTrue(limiter.tryAcquire()); assertFalse(limiter.tryAcquire()); assertFalse(limiter.tryAcquire()); }
    @Test void zeroLimitRejectsAllRequests() { assertFalse(new SlidingWindowRateLimiter(0,Duration.ofMinutes(1)).tryAcquire()); }
    @Test void instancesHaveIndependentLimits() { var a=new SlidingWindowRateLimiter(1,Duration.ofDays(1)); var b=new SlidingWindowRateLimiter(1,Duration.ofDays(1)); assertTrue(a.tryAcquire()); assertFalse(a.tryAcquire()); assertTrue(b.tryAcquire()); }
}
