package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SomeFile {

    @GetMapping("/health")
    public String health() {
        return "JVM Memory Stats: \n" +
                "Heap Size (MB): " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "\n" +
                "Non-Heap Size (MB): " + Runtime.getRuntime().nonHeapMemory() / (1024 * 1024);
    }

    public static void main(String[] args) {
        SpringApplication.run(SomeFile.class, args);
    }
}