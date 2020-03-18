package com.foxmail.chwetion.istio.demo.nextprovider.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;

@Controller
@RequestMapping("/nextprovider")
public class NextProviderController {

    private Random random = new Random();

    @GetMapping("/getNumber")
    @ResponseBody
    public String getNumber() {
        return "" + Math.abs(random.nextInt() % 100);
    }

    @GetMapping("/getString")
    @ResponseBody
    public String getString() {
        return "hello, world";
    }
}
