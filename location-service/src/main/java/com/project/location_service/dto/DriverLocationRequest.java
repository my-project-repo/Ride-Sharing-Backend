package com.project.location_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverLocationRequest {
    private String driverId; // Driver's id
    private double latitude; // Driver's Latitude
    private  double longitude; // Driver's Longitude
}
