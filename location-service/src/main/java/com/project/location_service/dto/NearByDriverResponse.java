package com.project.location_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NearByDriverResponse {
    private String driverId; // Driver's id
    private double latitude;
    private double longitude;
    private double distanceInKm; // Distance in Km from the requested Rider
}
