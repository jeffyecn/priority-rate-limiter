package com.yesup.debug;

import com.langcode.ratelimiter.PriorityRateLimiter;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

public class AllHigh {

    @Test
    public void test() {
        System.out.println("Testing all high traffic");

        PriorityRateLimiter rateLimiter = PriorityRateLimiter.create(100);

        RequestGenerator high = new RequestGenerator(90);

        Summary summary = new Summary();

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                summary.report();
            }
        }, 60000, 60000);

        high.startGenerating(300, ()->{
            summary.countHigh(rateLimiter.tryAcquireHigh());
        });

        high.waitFinish();

        summary.report();

        timer.cancel();
    }
}
