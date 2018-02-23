package com.yesup.debug;

import java.util.concurrent.atomic.AtomicLong;

public class Summary {

    final AtomicLong start_ts = new AtomicLong(System.currentTimeMillis());

    final AtomicLong highAllowCounter = new AtomicLong(0);
    final AtomicLong highRejectCounter = new AtomicLong(0);
    final AtomicLong lowAllowCounter = new AtomicLong(0);
    final AtomicLong logRejectCounter = new AtomicLong(0);

    public void countHigh(boolean allow) {
        if ( allow ) {
            highAllowCounter.incrementAndGet();
        } else {
            highRejectCounter.incrementAndGet();
        }
    }

    public void countLow(boolean allow) {
        if ( allow ) {
            lowAllowCounter.incrementAndGet();
        } else {
            logRejectCounter.incrementAndGet();
        }
    }

    public void report() {
        double span = (System.currentTimeMillis() - start_ts.getAndSet(System.currentTimeMillis()))/1000.0;

        long high_allow_num = highAllowCounter.getAndSet(0);
        long high_reject_num = highRejectCounter.getAndSet(0);
        long low_allow_num = lowAllowCounter.getAndSet(0);
        long low_reject_num = logRejectCounter.getAndSet(0);

        System.out.println("High QPS " + (high_allow_num + high_reject_num)/span);
        System.out.println("Low QPS " + (low_allow_num + low_reject_num)/span);
        System.out.println("All granted QPS " + (high_allow_num + low_allow_num)/span);
        System.out.println("High granted QPS " + high_allow_num/span);
        System.out.println("Low granted QPS " + low_allow_num/span);
    }
}
