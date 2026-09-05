package com.project.ride_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequest {
    @NotNull(message = "Rider Id is required")
    private String riderId;
    @NotNull(message = "Pickup Latitude is required")
    private double pickUpLatitude;
    @NotNull(message = "Pickup Longitude is required")
    private double pickUpLongitude;
    @NotNull(message = "PickUp Address is required")
    private String pickUpAddress;
    @NotNull(message = "Drop Latitude is required")
    private double dropLatitude;
    @NotNull(message = "Drop Longitude is required")
    private double dropLongitude;
    @NotNull(message = "Drop Address is required")
    private String dropAddress;
}
