package com.foxmail.chwetion.istio.demo.consumer.controller;

import com.foxmail.chwetion.istio.demo.consumer.client.ProviderClient;
import com.foxmail.chwetion.istio.demo.consumer.utils.TraceUtil;
import com.foxmail.chwetion.istiodemo.grpc.getmessage.GetMessageProto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Controller
@RequestMapping("/consumer")
public class ConsumerController {

    @Value("${upstream.provider.serviceAddr}")
    private String providerserviceAddr;
    @Value("${upstream.provider.servicePort}")
    private int providerServicePort;
    @Value("${upstream.grpcProvider.serviceAddr}")
    private String grpcProviderserviceAddr;
    @Value("${upstream.grpcProvider.servicePort}")
    private int grpcProviderServicePort;

    private final RestTemplate restTemplate;

    @Autowired
    public ConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * HTTP1 远程调用
     *
     * @param conCount  并发数
     * @param amount    访问次数
     * @param sleepTime 远程调用处理时间
     * @param code      远程调用响应码 200 or not 200
     * @param user      用户
     * @param headers   请求头
     * @return string
     * @author chwetion
     */
    @RequestMapping("/call")
    @ResponseBody
    public String call(int conCount, int amount, int sleepTime, int code, String user, @RequestHeader HttpHeaders headers) {
        // 构造请求
        HttpHeaders nextHeaders = new HttpHeaders();
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.put("sleepTime", Collections.singletonList(sleepTime));
        params.put("code", Collections.singletonList(code));
        if (user != null && !user.equals("")) {
            // create header
            nextHeaders.add("user", user);
        }
        // 链路传递
        TraceUtil.addTraceForHttp(headers, nextHeaders);
        HttpEntity httpEntity = new HttpEntity(params, nextHeaders);

        StringBuilder sb = new StringBuilder();
        ExecutorService executorService = Executors.newFixedThreadPool(conCount);
        List<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Future<String> future = executorService.submit(() -> {
                ResponseEntity<String> entity = restTemplate.postForEntity("http://" + providerserviceAddr + ":" + providerServicePort + "/provider/getMessage", httpEntity, String.class);

                if (!entity.getStatusCode().is2xxSuccessful()) {
                    return "调用失败，错误码：" + entity.getStatusCodeValue();
                }
                return entity.getBody();
            });
            results.add(future);
        }
        for (Future<String> result : results) {
            String message;
            try {
                message = result.get();
            } catch (InterruptedException | ExecutionException e) {
                message = "调用失败，未知错误";
                e.printStackTrace();
            }
            sb.append(message);
            sb.append("<br>");
        }
        sb.insert(0, "线程数：" + conCount + " 调用次数：" + results.size() + "<br>");
        executorService.shutdown();
        return sb.toString();
    }


    /**
     * GRPC 远程调用
     *
     * @param conCount  并发数
     * @param amount    访问次数
     * @param sleepTime 远程调用处理时间
     * @param code      远程调用响应码 200 or not 200
     * @param user      用户
     * @param headers   请求头
     * @return string
     * @author chwetion
     */
    @RequestMapping("/callgrpc")
    @ResponseBody
    public String callgrpc(int conCount, int amount, int sleepTime, int code, String user, @RequestHeader HttpHeaders headers) {
        // 构造请求
        HttpHeaders nextHeaders = new HttpHeaders();
        if (user != null && !user.equals("")) {
            nextHeaders.add("user", user);
        }
        // 链路传递
        TraceUtil.addTraceForHttp(headers, nextHeaders);

        StringBuilder sb = new StringBuilder();
        ExecutorService executorService = Executors.newFixedThreadPool(conCount);
        List<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Future<String> future = executorService.submit(() -> {
                ProviderClient providerClient = new ProviderClient(grpcProviderserviceAddr, grpcProviderServicePort, nextHeaders);
                GetMessageProto.GetMessageResponse message = providerClient.getMessage(sleepTime, code);
                if (!message.getCode().equals("")) {
                    return "" + message.getCode();
                } else {
                    return message.getMessage();
                }
            });
            results.add(future);
        }
        for (Future<String> result : results) {
            String message;
            try {
                message = result.get();
            } catch (InterruptedException | ExecutionException e) {
                message = "调用失败，未知错误";
            }
            sb.append(message);
            sb.append("<br>");
        }
        sb.insert(0, "线程数：" + conCount + " 调用次数：" + results.size() + "<br>");
        return sb.toString();
    }
}
