package com.yesup.debug;

import com.langcode.ratelimiter.PriorityRateLimiter;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

public class FiftyFifty {

    void doTest(double rate, double highQps, double lowQps) {
        PriorityRateLimiter rateLimiter = PriorityRateLimiter.create(rate);

        RequestGenerator high = new RequestGenerator(highQps);
        RequestGenerator low = new RequestGenerator(lowQps);

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

        low.startGenerating(300, ()->{
            summary.countLow(rateLimiter.tryAcquire());
        });

        high.waitFinish();
        low.waitFinish();

        summary.report();

        timer.cancel();
    }

    @Test
    public void testLow() {
        System.out.println("Testing low 50 - 50 traffic");

        doTest(100, 10, 10);
    }

    @Test
    public void testHigh() {
        System.out.println("Test high 50 - 50 traffic");

        doTest(100, 200, 200);
    }

    @Test
    public void testEdge() {
        System.out.println("Test edge 50 - 50 traffic");

        doTest(100, 60, 60);
    }
}
