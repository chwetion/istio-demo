package com.foxmail.chwetion.istio.demo.consumer.interceptor;

import io.grpc.*;
import org.springframework.http.HttpHeaders;

public class ClientHeaderInterceptor implements ClientInterceptor {

    private HttpHeaders nextHeaders;

    public ClientHeaderInterceptor(HttpHeaders nextHeaders) {
        this.nextHeaders = nextHeaders;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                if (nextHeaders != null) {
                    for (String key : nextHeaders.keySet()) {
                        headers.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), String.valueOf(nextHeaders.get(key)));
                    }
                    super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                        @Override
                        public void onHeaders(Metadata headers) {
                            super.onHeaders(headers);
                        }
                    }, headers);
                }
            }
        };
    }
}
