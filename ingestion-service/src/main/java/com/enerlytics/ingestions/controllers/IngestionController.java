package com.enerlytics.ingestions.controllers;

import com.enerlytics.ingestions.dtos.requests.IngestionRequest;
import com.enerlytics.ingestions.services.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
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
@Tag(name = "Ingestion", description = "Operations for ingesting device energy readings")
public class IngestionController {

    private final IngestionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ingest energy reading", description = "Publishes a single device energy usage event.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ingestion accepted"),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void ingestData(@RequestBody @Valid IngestionRequest request) {
        service.ingestDate(request);
    }
}
