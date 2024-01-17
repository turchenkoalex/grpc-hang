package com.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class TestServer implements AutoCloseable {
    private static final Logger log = Logger.getLogger(TestServer.class.getCanonicalName());

    private final Server server;

    public TestServer(int port) {
        server = ServerBuilder.forPort(port)
                .addService(new ExampleServiceImpl())
                .build();
    }

    public void start() throws IOException {
        server.start();
        log.info("Server started at *:" + server.getPort());
    }

    @Override
    public void close() {
        log.info("Server shutdown");
        server.shutdown();
        try {
            server.awaitTermination();
            log.info("Server shutdown done");
        } catch (InterruptedException ignore) {
            // nothing to do
        }
    }
}
