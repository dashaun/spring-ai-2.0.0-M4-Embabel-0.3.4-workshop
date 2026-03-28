package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    public record Greeting(String message, long timestamp) {}

    @GetMapping("/greet")
    public Greeting greet(@RequestParam(defaultValue = "World") String name) {
        return new Greeting("Hello, %s!".formatted(name), System.currentTimeMillis());
    }
}
