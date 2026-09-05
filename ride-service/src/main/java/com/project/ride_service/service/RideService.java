package com.project.ride_service.service;

import com.project.ride_service.dto.RideRequest;
import com.project.ride_service.dto.RideResponse;
import com.project.ride_service.event.RideRequestedEvent;
import com.project.ride_service.model.Ride;
import com.project.ride_service.model.RideStatus;
import com.project.ride_service.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {

    private final RideRepository rideRepository;
    private  final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;
    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";

    public  RideResponse requestRide(RideRequest request) {
        Ride ride = new Ride();
        ride.setRiderId(request.getRiderId());
        ride.setPickUpLatitude(request.getPickUpLatitude());
        ride.setPickUpLongitude(request.getPickUpLongitude());
        ride.setPickUpAddress(request.getPickUpAddress());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());
        ride.setDropAddress(request.getDropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimateFare(request));
        Ride savedRide = rideRepository.save(ride);

        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getRiderId(),
                savedRide.getPickUpLatitude(),
                savedRide.getPickUpLongitude(),
                savedRide.getPickUpAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC,savedRide.getId(),event);
        savedRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);
        return mapToResponse(savedRide);
    }

    public  RideResponse getRideById(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()-> new RuntimeException("Ride not found"));
        return mapToResponse(ride);
    }

    public  List<RideResponse> getRidesByRider(String riderId) {
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RideResponse startRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()->new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.ACCEPTED)
        {
            throw new RuntimeException("Ride cannot be started as it has not been accepted");
        }

        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        Ride savedRide = rideRepository.save(ride);
        return mapToResponse(savedRide);
    }

    public RideResponse completeRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()->new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.RIDE_STARTED)
        {
            throw new RuntimeException("Ride has not been started or accepted");
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        Ride savedRide = rideRepository.save(ride);
        return mapToResponse(savedRide);
    }

    public RideResponse cancelRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()->new RuntimeException("Ride not found"));

        ride.setStatus(RideStatus.CANCELLED);
        Ride savedRide = rideRepository.save(ride);
        return mapToResponse(savedRide);
    }

    public void updateRideWithDriver (String rideId , String driverId)
    {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()->new RuntimeException("Ride not found"));

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);
    }

    private RideResponse mapToResponse(Ride ride) {
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickUpLatitude(ride.getPickUpLatitude());
        response.setPickUpLongitude(ride.getPickUpLongitude());
        response.setPickUpAddress(ride.getPickUpAddress());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());
        response.setStatus(ride.getStatus());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setCreatedAt(ride.getCreatedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        return response;
    }
    private double calculateEstimateFare(RideRequest request) {
        //Haversine formula to calculate distance

        double R = 6371.0; // Earth radius in km

        double pickupLat = Math.toRadians(request.getPickUpLatitude());
        double pickupLon = Math.toRadians(request.getPickUpLongitude());

        double dropLat = Math.toRadians(request.getDropLatitude());
        double dropLon = Math.toRadians(request.getDropLongitude());

        double dLat = dropLat - pickupLat;
        double dLon = dropLon - pickupLon;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(pickupLat) * Math.cos(dropLat)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distanceKm = R * c;

        double baseFare = 50;
        double perKm = 15;

        return baseFare + (distanceKm * perKm);
    }
}
