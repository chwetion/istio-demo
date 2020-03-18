package com.foxmail.chwetion.istio.demo.grpcprovider;


import com.foxmail.chwetion.istiodemo.grpc.getmessage.GetMessageGrpc;
import com.foxmail.chwetion.istiodemo.grpc.getmessage.GetMessageProto;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Random;

public class ProviderController {
    private final int PORT = 8888;
    private Server server;

    private void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(new GetMessage())
                .build()
                .start();
        System.out.println("service start");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("shutting down grpc server");
                ProviderController.this.stop();
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private static class GetMessage extends GetMessageGrpc.GetMessageImplBase {
        @Override
        public void getMessage(GetMessageProto.GetMessageRequest request, StreamObserver<GetMessageProto.GetMessageResponse> responseObserver) {
            int sleepTime = request.getSleepTime();
            int code = request.getCode();
            try {
                Thread.sleep(sleepTime * 1000);
            } catch (InterruptedException e) {
                responseObserver.onError(e);
            }
            if (code != 200) {
                responseObserver.onError(new Exception("bussiness exception"));
            }
            Random random = new Random();
            responseObserver.onNext(GetMessageProto.GetMessageResponse.newBuilder().setMessage("access success and random number is: " + Math.abs(random.nextInt() % 100)).build());
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final ProviderController controller = new ProviderController();
        controller.start();
        controller.blockUntilShutdown();
    }
}
