package com.enerlytics.devices.controllers;

import com.enerlytics.devices.dtos.requests.CreateDeviceRequest;
import com.enerlytics.devices.dtos.requests.UpdateDeviceRequest;
import com.enerlytics.devices.dtos.responses.DeviceResponse;
import com.enerlytics.devices.services.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/devices")
@Tag(name = "Devices", description = "Operations for creating and managing devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @Operation(summary = "Create device", description = "Registers a new device for a user.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Device created",
                content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by id", description = "Retrieves a single device's details by its ID.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Device found",
                content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Device not found",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<DeviceResponse> getDeviceById(
            @Parameter(description = "Device id", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @GetMapping
    @Operation(summary = "List devices", description = "Returns a list of all registered devices.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Devices fetched",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeviceResponse.class))))
    })
    public ResponseEntity<List<DeviceResponse>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update device", description = "Updates details of an existing device.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Device updated",
                content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Device not found",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<DeviceResponse> updateDevice(
            @Parameter(description = "Device id", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateDeviceRequest request) {
        return ResponseEntity.ok(deviceService.updateDevice(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete device", description = "Deletes a device record from the system.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Device deleted"),
        @ApiResponse(
                responseCode = "404",
                description = "Device not found",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "Device id", example = "1") @PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "List devices by user id",
            description = "Retrieves all devices associated with a specific user.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Devices fetched for user",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeviceResponse.class))))
    })
    public ResponseEntity<List<DeviceResponse>> getAllDevicesByUserId(
            @Parameter(description = "User id", example = "1") @PathVariable Long userId) {
        return ResponseEntity.ok(deviceService.getAllDevicesByUserId(userId));
    }
}
