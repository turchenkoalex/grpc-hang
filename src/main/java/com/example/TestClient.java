package com.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestClient {
    private static final Logger log = Logger.getLogger(TestClient.class.getCanonicalName());
    private final long callTimeoutMillis;
    private final ExampleServiceGrpc.ExampleServiceBlockingStub blockingStub;

    public TestClient(int port, Duration callTimeout, boolean enableRetry, Map<String, ?> serviceConfig) {
        callTimeoutMillis = callTimeout.toMillis();
        var builder = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .disableServiceConfigLookUp()
                .defaultServiceConfig(serviceConfig);

        if (enableRetry) {
            builder.enableRetry();
        } else {
            builder.disableRetry();
        }

        ManagedChannel channel = builder.build();

        blockingStub = ExampleServiceGrpc.newBlockingStub(channel);
    }

    public void callWithError() {
        call(true);
    }

    public void callWithOK() {
        call(false);
    }

    private void call(boolean withError) {
        log.info("Client: request withError=" + withError);

        ExampleProto.Req req = ExampleProto.Req.newBuilder()
                .setReturnError(withError)
                .build();

        try {
            ExampleProto.Resp ignore = blockingStub
                    .withDeadlineAfter(callTimeoutMillis, TimeUnit.MILLISECONDS)
                    .unaryCall(req);

            log.info("Client: OK");
        } catch (Exception e) {
            log.log(Level.WARNING, "Client: call failure", e);
        }
    }
}
