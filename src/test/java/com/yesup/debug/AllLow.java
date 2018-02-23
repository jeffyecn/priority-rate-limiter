package com.yesup.debug;

import com.langcode.ratelimiter.PriorityRateLimiter;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

public class AllLow {

    @Test
    public void test() {
        System.out.println("Testing all low traffic");

        PriorityRateLimiter rateLimiter = PriorityRateLimiter.create(100);

        RequestGenerator low = new RequestGenerator(120);

        Summary summary = new Summary();

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                summary.report();
            }
        }, 60000, 60000);

        low.startGenerating(300, ()->{
            summary.countLow(rateLimiter.tryAcquire());
        });

        low.waitFinish();

        summary.report();

        timer.cancel();
    }
}
