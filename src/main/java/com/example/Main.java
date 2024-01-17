package com.example;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This test shows a hang error on a blocking grpc call if the client uses the retry policy and uses a deadline
 * Retry policy described in TestClient class.
 *
 *
 * <pre>
 * Hanged thread stack
 *
 * "pool-1-thread-1" #22 [29443] prio=5 os_prio=31 cpu=34.13ms elapsed=34.56s tid=0x00000001408fd000 nid=29443 waiting on condition  [0x0000000171c4e000]
 *    java.lang.Thread.State: WAITING (parking)
 * 	at jdk.internal.misc.Unsafe.park(java.base@21.0.1/Native Method)
 * 	- parking to wait for  <0x000000061f08eb00> (a io.grpc.stub.ClientCalls$ThreadlessExecutor)
 * 	at java.util.concurrent.locks.LockSupport.park(java.base@21.0.1/LockSupport.java:221)
 * 	at io.grpc.stub.ClientCalls$ThreadlessExecutor.waitAndDrain(ClientCalls.java:717)
 * 	at io.grpc.stub.ClientCalls.blockingUnaryCall(ClientCalls.java:159)
 * 	at com.example.ExampleServiceGrpc$ExampleServiceBlockingStub.unaryCall(ExampleServiceGrpc.java:160)
 * 	at com.example.TestClient.call(TestClient.java:63)
 * 	at com.example.TestRunner.lambda$run$0(TestRunner.java:20)
 * 	at com.example.TestRunner$$Lambda/0x00000070010e9dd8.run(Unknown Source)
 * 	at java.util.concurrent.Executors$RunnableAdapter.call(java.base@21.0.1/Executors.java:572)
 * 	at java.util.concurrent.FutureTask.runAndReset(java.base@21.0.1/FutureTask.java:358)
 * 	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(java.base@21.0.1/ScheduledThreadPoolExecutor.java:305)
 * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(java.base@21.0.1/ThreadPoolExecutor.java:1144)
 * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(java.base@21.0.1/ThreadPoolExecutor.java:642)
 * 	at java.lang.Thread.runWith(java.base@21.0.1/Thread.java:1596)
 * 	at java.lang.Thread.run(java.base@21.0.1/Thread.java:1583)
 * </pre>
 *
 * @see TestClient To configure the client
 * @see TestServer To configure the server
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getCanonicalName());

    private static final int GRPC_PORT = 9999;

    public static void main(String[] args) throws IOException, InterruptedException {
        Duration grpcCallTimeout = Duration.ofMillis(100);
        Duration callFrequency = Duration.ofMillis(100);

        try (TestRunner testRunner = new TestRunner(GRPC_PORT, grpcCallTimeout)) {
            // start grpc server
            try (TestServer server = new TestServer(GRPC_PORT)) {
                server.start();

                // start thread with grpc calls every 100ms
                testRunner.run(callFrequency);

                // wait for first 10 success grpc calls
                log.info("Wait for 10 success calls");
                testRunner.awaitSuccessCalls(10, Duration.ofSeconds(15));

                // shutdown grpc server for emulate server unavailable
                log.info("Server goes away");
            }

            // wait for 1 second, before restart server
            // emulate 1 second server unavailable
            TimeUnit.SECONDS.sleep(1);

            try (TestServer server = new TestServer(GRPC_PORT)) {
                server.start();

                // wait for next 10 success calls
                testRunner.awaitSuccessCalls(100, Duration.ofSeconds(15));
            }
        }

        // if bug happens, this line never called
        log.info("Test is OK");
    }
}
