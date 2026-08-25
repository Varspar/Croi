package com.croi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Croi AI Workforce Platform - Main Application Entry Point
 */
@SpringBootApplication
@EnableAsync
public class CroiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CroiApplication.class, args);
    }
}