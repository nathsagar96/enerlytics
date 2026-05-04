package com.enerlytics.insights.controllers;

import com.enerlytics.insights.dtos.responses.InsightResponse;
import com.enerlytics.insights.services.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/insights")
@Tag(name = "Insights", description = "Operations for AI-generated usage insights")
public class InsightController {

    private final InsightService service;

    @GetMapping("/saving-tips/{userId}")
    @Operation(summary = "Get saving tips", description = "Returns personalized energy-saving tips for a user.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Saving tips generated",
                content = @Content(schema = @Schema(implementation = InsightResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "Insight generation failed",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<InsightResponse> getSavingTips(
            @Parameter(description = "User id", example = "1") @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getSavingTips(userId));
    }

    @GetMapping("/overview/{userId}")
    @Operation(summary = "Get usage overview", description = "Returns an AI-generated overview for recent usage.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Overview generated",
                content = @Content(schema = @Schema(implementation = InsightResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "Insight generation failed",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<InsightResponse> getOverview(
            @Parameter(description = "User id", example = "1") @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getOverview(userId));
    }
}
