package com.enerlytics.ingestions.controllers;

import com.enerlytics.ingestions.dtos.requests.IngestionRequest;
import com.enerlytics.ingestions.services.IngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ingestions")
public class IngestionController {

    private final IngestionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void ingestData(@RequestBody @Valid IngestionRequest request) {
        service.ingestDate(request);
    }
}
