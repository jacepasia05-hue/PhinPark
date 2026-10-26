package com.phinpark.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phinpark.dto.ApiResponse;
import com.phinpark.dto.VehicleResponse;
import com.phinpark.entity.UserEntity;
import com.phinpark.service.VehicleService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles() {
        try {
            List<VehicleResponse> vehicles = vehicleService.getAllVehicles();
            return ResponseEntity.ok(ApiResponse.success(
                    "Retrieved " + vehicles.size() + " vehicles successfully",
                    vehicles,
                    HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving vehicles",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> registerVehicle(
            @RequestBody com.phinpark.dto.VehicleRequest request,
            HttpServletRequest httpRequest) {
        try {
            UserEntity authenticatedUser = (UserEntity) httpRequest.getAttribute("authenticatedUser");
            int userId = authenticatedUser.getId();
            if (userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User ID is required",
                                HttpStatus.BAD_REQUEST.value(), "Missing userId parameter"));
            }
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Vehicle data is required",
                                HttpStatus.BAD_REQUEST.value(), "Request body is empty"));
            }
            VehicleResponse response = vehicleService.registerVehicle(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Vehicle registered successfully", response, HttpStatus.CREATED.value()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
                            "Vehicle registration failed"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getUserVehicles(@PathVariable int userId,
            HttpServletRequest httpRequest) {
        try {
            UserEntity authenticatedUser = (UserEntity) httpRequest.getAttribute("authenticatedUser");
            if (!"ADMIN".equalsIgnoreCase(authenticatedUser.getAccountType())
                    && authenticatedUser.getId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only view your own vehicles",
                                HttpStatus.FORBIDDEN.value(), "User access denied"));
            }
            if (userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid user ID",
                                HttpStatus.BAD_REQUEST.value(), "User ID must be positive"));
            }

            List<VehicleResponse> vehicles = vehicleService.getUserVehicles(userId);
            return ResponseEntity.ok(ApiResponse.success(
                    "Retrieved " + vehicles.size() + " vehicles for user " + userId,
                    vehicles,
                    HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(),
                            HttpStatus.BAD_REQUEST.value(), "Error retrieving user vehicles"));
        }
    }

    @DeleteMapping("/{plateNumber}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable String plateNumber) {
        try {
            vehicleService.deleteVehicle(plateNumber, true);
            return ResponseEntity.ok(ApiResponse.success(
                    "Vehicle removed successfully", null, HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(),
                            "Vehicle removal failed"));
        }
    }
}
