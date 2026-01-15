package com.enerlytics.ingestions.controllers;

import com.enerlytics.ingestions.dtos.requests.EnergyUsageRequest;
import com.enerlytics.ingestions.services.IngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ingestions")
public class IngestionController {
    private final IngestionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void ingestData(@Valid @RequestBody EnergyUsageRequest request) {
        service.ingestEnergyUsage(request);
    }
}
