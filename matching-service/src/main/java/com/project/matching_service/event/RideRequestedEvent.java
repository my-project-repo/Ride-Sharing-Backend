package com.project.matching_service.event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class RideRequestedEvent {
    private String rideId;
    private String riderId;

    private double pickUpLatitude;
    private double pickUpLongitude;
    private String pickUpAddress;


    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;


}
