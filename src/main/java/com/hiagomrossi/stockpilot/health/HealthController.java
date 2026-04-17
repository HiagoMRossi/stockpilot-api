package com.hiagomrossi.stockpilot.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "ok",
                "service", "StockPilot API"
        );
    }

    @GetMapping("/api/v1/info")
    public Map<String, String> info() {
        return Map.of(
                "name", "StockPilot API",
                "version", "v1",
                "status", "running"
        );
    }
}