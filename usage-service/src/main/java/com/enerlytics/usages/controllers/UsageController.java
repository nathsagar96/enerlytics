package com.enerlytics.usages.controllers;

import com.enerlytics.usages.dtos.responses.UsageResponse;
import com.enerlytics.usages.services.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/usages")
public class UsageController {

    private final UsageService usageService;

    @GetMapping("/{userId}")
    public ResponseEntity<UsageResponse> getUserDeviceUsage(
            @PathVariable Long userId, @RequestParam(defaultValue = "3") int days) {
        return ResponseEntity.status(HttpStatus.OK).body(usageService.getUsageForUserOverDays(userId, days));
    }
}
