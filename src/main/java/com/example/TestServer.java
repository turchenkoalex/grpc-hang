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

        try {
            server.start();
            log.info("Server started at *:" + server.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        log.info("Server stop");
        server.shutdown();
        try {
            server.awaitTermination();
            log.info("Server stopped");
        } catch (InterruptedException ignore) {
            // nothing to do
        }
    }
}
