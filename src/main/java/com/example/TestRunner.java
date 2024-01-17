package com.example;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestRunner implements AutoCloseable {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private volatile CountDownLatch latch = new CountDownLatch(1);
    private final TestClient client;

    public TestRunner(int port, Duration callTimeout) {
        client = new TestClient(port, callTimeout);
    }

    public void run(Duration frequency) {
        executor.scheduleWithFixedDelay(() -> {
            if (client.call()) {
                latch.countDown();
            }
        }, frequency.toMillis(), frequency.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void awaitSuccessCalls(int successCalls, Duration duration) throws InterruptedException {
        latch = new CountDownLatch(successCalls);
        if (!latch.await(duration.toMillis(), TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("Timeout error while waiting for success calls. " + latch.getCount() + " calls left.");
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
