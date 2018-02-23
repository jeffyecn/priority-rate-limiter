package com.langcode.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class PriorityRateLimiter {

    private final static Logger LOG = LoggerFactory.getLogger(PriorityRateLimiter.class);

    public static PriorityRateLimiter create(double permitsPerSecond) {
        return create(permitsPerSecond, 0.8);
    }

    public static PriorityRateLimiter create(double permitsPerSecond, double maxReservePercentage) {
        if ( maxReservePercentage >= 1 || maxReservePercentage < 0) {
            throw new IllegalArgumentException("reserve percentage must between 0 and 1");
        }
        return new PriorityRateLimiter(permitsPerSecond, maxReservePercentage);
    }

    protected final double minReservePercentage = 0.1;
    protected final double maxReservePercentage;
    protected final RateLimiter highPriorityRateLimiter;
    protected final RateLimiter regularRateLimiter;
    protected final long checkReserveInterval = 15 * 1000; // check reserved every 15 seconds

    protected volatile double totalPermitsPerSecond;
    protected volatile double reserveRate;
    protected volatile long nextCheckTime;

    protected final AtomicLong highPriorityTriedCounter = new AtomicLong(0);

    protected PriorityRateLimiter(double permitsPerSecond, double maxReservePercentage) {
        totalPermitsPerSecond = permitsPerSecond;
        this.maxReservePercentage = maxReservePercentage;
        reserveRate = maxReservePercentage;

        highPriorityRateLimiter = RateLimiter.create(totalPermitsPerSecond * reserveRate);
        regularRateLimiter = RateLimiter.create(totalPermitsPerSecond * (1-reserveRate));
        nextCheckTime = System.currentTimeMillis() + checkReserveInterval;
    }

    public boolean tryAcquireHigh() {
        highPriorityTriedCounter.incrementAndGet();
        boolean passed = highPriorityRateLimiter.tryAcquire();
        if ( ! passed ) {
            passed = regularRateLimiter.tryAcquire();
        }
        updateReserveIfNeed();
        return passed;
    }

    public boolean tryAcquire() {
        boolean passed = regularRateLimiter.tryAcquire();
        updateReserveIfNeed();
        return passed;
    }

    public void setRate(double newQPS) {
        applyReserveRate(newQPS, reserveRate);
    }

    protected void updateReserveIfNeed() {
        if ( System.currentTimeMillis() < nextCheckTime ) {
            boolean needUpdate = false;
            long span = 0;
            synchronized (this) {
                if ( System.currentTimeMillis() < nextCheckTime ) {
                    span = System.currentTimeMillis() - nextCheckTime + checkReserveInterval;
                    nextCheckTime = System.currentTimeMillis() + checkReserveInterval;
                    needUpdate = true;
                }
            }
            if ( needUpdate ) {
                updateReserve(span);
            }
        }
    }

    protected void updateReserve(long span) {
        double highPriorityQps = highPriorityTriedCounter.getAndSet(0) * 1000 / (double) span;
        double newReserveRate = highPriorityQps / totalPermitsPerSecond;
        if ( newReserveRate > maxReservePercentage ) {
            newReserveRate = maxReservePercentage;
        }
        if ( newReserveRate < minReservePercentage ) {
            newReserveRate = minReservePercentage;
        }
        applyReserveRate(totalPermitsPerSecond, newReserveRate);
    }

    protected void applyReserveRate(double newQPS, double newRate) {
        synchronized (this) {
            totalPermitsPerSecond = newQPS;
            reserveRate = newRate;
            highPriorityRateLimiter.setRate(totalPermitsPerSecond * reserveRate);
            regularRateLimiter.setRate(totalPermitsPerSecond*(1-reserveRate));
        }
    }
}
