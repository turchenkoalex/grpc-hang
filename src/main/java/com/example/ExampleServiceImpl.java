package com.example;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class ExampleServiceImpl extends ExampleServiceGrpc.ExampleServiceImplBase {
    private static final Logger log = Logger.getLogger(ExampleServiceImpl.class.getCanonicalName());

    @Override
    public void unaryCall(ExampleProto.Req request, StreamObserver<ExampleProto.Resp> responseObserver) {
        log.info("Server: Received returnError:" + request.getReturnError());

        if (request.getReturnError()) {
            log.info("Server: Response status UNKNOWN");
            responseObserver.onError(Status.UNKNOWN.asRuntimeException());
            return;
        }

        log.info("Server: Response status OK");
        responseObserver.onNext(ExampleProto.Resp.newBuilder().build());
        responseObserver.onCompleted();
    }
}
