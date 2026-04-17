package com.nitinconstructions.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HealthCheckController {

    @GetMapping("/HbtChk")
    public String hbtCheck() {
        log.info("Application is Active !!");
        return "Application is Running";
    }
}
