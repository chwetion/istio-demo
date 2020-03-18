package com.foxmail.chwetion.istio.demo.consumer.client;

import com.foxmail.chwetion.istio.demo.consumer.interceptor.ClientHeaderInterceptor;
import com.foxmail.chwetion.istiodemo.grpc.getmessage.GetMessageGrpc;
import com.foxmail.chwetion.istiodemo.grpc.getmessage.GetMessageProto;
import io.grpc.*;
import org.springframework.http.HttpHeaders;

public class ProviderClient {
    private final ManagedChannel channel;
    private final GetMessageGrpc.GetMessageBlockingStub blockingStub;

    public ProviderClient(String host, int port, HttpHeaders headers) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        Channel channelWithHeaders = ClientInterceptors.intercept(this.channel, new ClientHeaderInterceptor(headers));
        blockingStub = GetMessageGrpc.newBlockingStub(channelWithHeaders);
    }

    public GetMessageProto.GetMessageResponse getMessage(int sleepTime, int code) {
        GetMessageProto.GetMessageRequest request = GetMessageProto.GetMessageRequest.newBuilder()
                .setSleepTime(sleepTime).setCode(code).build();
        GetMessageProto.GetMessageResponse response;
        try {
            response = blockingStub.getMessage(request);
        } catch (StatusRuntimeException e) {
            response = GetMessageProto.GetMessageResponse.newBuilder()
                    .setCode(e.getStatus().getCode().toString()).build();
        }
        return response;
    }
}
