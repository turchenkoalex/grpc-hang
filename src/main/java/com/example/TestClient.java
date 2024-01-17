package com.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestClient {
    private static final Logger log = Logger.getLogger(TestClient.class.getCanonicalName());
    private final AtomicInteger counter = new AtomicInteger(1);
    private final long callTimeoutMillis;
    private final ManagedChannel channel;
    private final ExampleServiceGrpc.ExampleServiceBlockingStub blockingStub;

    public TestClient(int port, Duration callTimeout) {
        callTimeoutMillis = callTimeout.toMillis();
        channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .disableServiceConfigLookUp()
                .enableRetry()
                // If disable retries, all works fine
//                 .disableRetry()
                .maxRetryAttempts(5)
                .defaultServiceConfig(
                        Map.of("methodConfig",
                                List.of(
                                        Map.of(
                                                "name", List.of(Map.of()),
                                                "retryPolicy", Map.of(
                                                        "maxAttempts", 5.0,
                                                        "initialBackoff", "0.5s",
                                                        "maxBackoff", "3.0s",
                                                        "backoffMultiplier", 1.3,
                                                        "retryableStatusCodes", List.of("UNAVAILABLE")
                                                )
                                        )
                                )
                        )
                )
                .build();

        blockingStub = ExampleServiceGrpc.newBlockingStub(channel);
    }

    public boolean call() {
        final int num = counter.getAndIncrement();

        log.info("REQ " + num);

        ExampleProto.Req req = ExampleProto.Req.newBuilder()
                .setNum(num)
                .build();

        try {
            ExampleProto.Resp resp = blockingStub.withDeadlineAfter(callTimeoutMillis, TimeUnit.MILLISECONDS)
                    .unaryCall(req);

            log.info("RESP " + resp.getNum());
            return resp.getNum() == num;
        } catch (Exception e) {
            log.log(Level.WARNING, "call " + num + " failure", e);
            return false;
        }
    }
}
