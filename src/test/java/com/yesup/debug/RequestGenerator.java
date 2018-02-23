package com.yesup.debug;

import com.google.common.util.concurrent.RateLimiter;

public class RequestGenerator {

    private final RateLimiter rateLimiter;
    private Thread thread;

    public RequestGenerator(double qps) {
        rateLimiter = RateLimiter.create(qps);
    }

    public void startGenerating(long time_in_second, Runnable onEvent) {
        long end_ts = System.currentTimeMillis() + time_in_second * 1000;
        thread = new Thread(()->{
            while ( System.currentTimeMillis() < end_ts ) {
                rateLimiter.acquire();
                onEvent.run();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void waitFinish() {
        try {
            thread.join();
        } catch ( InterruptedException ex) {

        }
    }
}
