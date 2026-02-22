package com.example.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/v1/health")
    public Map<String, String> healthCheck() {
        return Map.of(
            "status", "UP",
            "service", "gacha-stockout-backend",
            "time", OffsetDateTime.now().toString()
        );
    } 
}
