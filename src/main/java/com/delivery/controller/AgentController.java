package com.delivery.controller;

import com.delivery.service.DeliveryTrackingAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AgentController {

    private final DeliveryTrackingAgentService deliveryTrackingAgentService;

    @PostMapping("/agent/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");
        String reply = deliveryTrackingAgentService.handleRequest(message);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
