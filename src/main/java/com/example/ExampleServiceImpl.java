package com.example;

import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class ExampleServiceImpl extends ExampleServiceGrpc.ExampleServiceImplBase {
    private static final Logger log = Logger.getLogger(ExampleServiceImpl.class.getCanonicalName());

    @Override
    public void unaryCall(ExampleProto.Req request, StreamObserver<ExampleProto.Resp> responseObserver) {
        log.info("RCVD "+ request.getNum());

        ExampleProto.Resp resp = ExampleProto.Resp.newBuilder()
                .setNum(request.getNum())
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
