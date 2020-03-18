package com.foxmail.chwetion.istio.demo.provider.controller;

import com.foxmail.chwetion.istio.demo.provider.utils.TraceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/provider")
public class ProviderController {
    @Value("${upstream.nextprovider.serviceAddr}")
    private String nextproviderserviceAddr;
    @Value("${upstream.nextprovider.servicePort}")
    private String nextproviderServicePort;

    private final RestTemplate restTemplate;

    @Autowired
    public ProviderController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/getMessage")
    @ResponseBody
    public String getMessage(int sleepTime, int code, @RequestHeader HttpHeaders headers) throws Exception {
        Thread.sleep(sleepTime * 1000);
        if (code != 200) {
            throw new Exception("bussiness exception");
        }
        // remote call
        HttpHeaders nextHeaders = new HttpHeaders();
        TraceUtil.addTraceForHttp(headers, nextHeaders);
        if (headers.get("user") != null) {
            nextHeaders.put("user", headers.get("user"));
        }
        HttpEntity httpEntity = new HttpEntity(nextHeaders);
        String result;
        ResponseEntity<String> entity = restTemplate.exchange("http://" + nextproviderserviceAddr + ":" + nextproviderServicePort + "/nextprovider/getNumber", HttpMethod.GET, httpEntity, String.class);
        int responseCode = entity.getStatusCodeValue();
        if (responseCode != 200) {
            result = "nextProvider调用失败，错误码：" + responseCode;
        } else {
            result = entity.getBody();
        }
        // return result
        return "access success and random number is: " + result;
    }
}
