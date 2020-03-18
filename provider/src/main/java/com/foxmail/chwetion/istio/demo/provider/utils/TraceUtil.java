package com.foxmail.chwetion.istio.demo.provider.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

import java.util.List;

public class TraceUtil {
    private static final String[] keys = {
            "x-request-id",
            "x-b3-traceid",
            "x-b3-spanid",
            "x-b3-parentspanid",
            "x-b3-sampled",
            "x-b3-flags",
            "x-ot-span-context"
    };

    public static void addTraceForHttp(HttpHeaders headers, HttpHeaders nextHeaders) {
        Assert.notNull(headers, "param headers is not allow null");
        Assert.notNull(nextHeaders, "param nextHeaders is not allow null");
        for (String key : keys) {
            List<String> value = headers.get(key);
            if (value != null)
                nextHeaders.put(key, value);
        }
    }
}