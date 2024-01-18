package com.example;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This test shows a hang error on a blocking grpc call if the client uses the retry policy and uses a deadline
 * Retry policy described in TestClient class.
 *
 *
 * <pre>
 * Hanged thread stack
 *
 *"main" #1 [8707] prio=5 os_prio=31 cpu=207.44ms elapsed=16.32s tid=0x0000000132808200 nid=8707 waiting on condition  [0x000000016fcda000]
 *    java.lang.Thread.State: WAITING (parking)
 * 	at jdk.internal.misc.Unsafe.park(java.base@21.0.1/Native Method)
 * 	- parking to wait for  <0x000000061e400010> (a io.grpc.stub.ClientCalls$ThreadlessExecutor)
 * 	at java.util.concurrent.locks.LockSupport.park(java.base@21.0.1/LockSupport.java:221)
 * 	at io.grpc.stub.ClientCalls$ThreadlessExecutor.waitAndDrain(ClientCalls.java:717)
 * 	at io.grpc.stub.ClientCalls.blockingUnaryCall(ClientCalls.java:159)
 * 	at com.example.ExampleServiceGrpc$ExampleServiceBlockingStub.unaryCall(ExampleServiceGrpc.java:160)
 * 	at com.example.TestClient.call(TestClient.java:70)
 * 	at com.example.TestClient.callWithError(TestClient.java:53)
 * 	at com.example.Main.main(Main.java:52)
 * </pre>
 *
 * @see TestClient To configure the client
 * @see TestServer To configure the server
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getCanonicalName());

    private static final int GRPC_PORT = 9999;

    public static void main(String[] args) {
        final boolean retires = true;

        // Backoff + ..., for reproducing bug must be less than all retries deadline (5 * backoff)
        Duration deadline = Duration.ofSeconds(1);
        Map<String, ?> serviceConfig = Map.of("methodConfig",
                List.of(
                        Map.of(
                                "name", List.of(Map.of()),
                                "retryPolicy", Map.of(
                                        "maxAttempts", 4D,
                                        "initialBackoff", "10s",
                                        "maxBackoff", "10s",
                                        "backoffMultiplier", 1D,
                                        "retryableStatusCodes", List.of("UNKNOWN")
                                )
                        )
                )
        );

        TestClient client = new TestClient(GRPC_PORT, deadline, retires, serviceConfig);

        try (TestServer ignored = new TestServer(GRPC_PORT)) {

            // Test success call, no retries
            client.callWithOK();

            // hang on this client call, retry occurs
            client.callWithError();

            // Never happens if bug present (reties + deadline configuration)
            log.info("Test done");
        }
    }
}
