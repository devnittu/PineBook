package com.pinebook.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * PineBook API — Spring Boot entry point.
 * Async enabled for non-blocking video processing dispatch.
 */
@SpringBootApplication
@EnableAsync
public class PineApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PineApiApplication.class, args);
    }
}
