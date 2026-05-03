package com.enerlytics.insights.controllers;

import com.enerlytics.insights.dtos.responses.InsightResponse;
import com.enerlytics.insights.services.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/insights")
public class InsightController {

    private final InsightService service;

    @GetMapping("/saving-tips/{userId}")
    public ResponseEntity<InsightResponse> getSavingTips(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getSavingTips(userId));
    }

    @GetMapping("/overview/{userId}")
    public ResponseEntity<InsightResponse> getOverview(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getOverview(userId));
    }
}
