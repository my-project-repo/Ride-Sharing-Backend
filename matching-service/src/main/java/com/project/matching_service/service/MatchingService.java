package com.project.matching_service.service;

import com.project.matching_service.client.LocationServiceClient;
import com.project.matching_service.dto.NearByDriverResponse;
import com.project.matching_service.event.RideMatchedEvent;
import com.project.matching_service.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {
    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;
    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_RADIUS = 5.0;

    public void matchDriverForRide (RideRequestedEvent event)
    {
        log.warn("EVENT INFO lat = {} long = {}",event.getPickUpLatitude(),event.getPickUpLongitude());
        List<NearByDriverResponse> nearByDrivers = locationServiceClient.getNearByDrivers(
                event.getPickUpLatitude(),
                event.getPickUpLongitude(),
                DEFAULT_RADIUS
        );

        if (nearByDrivers.isEmpty()) {
            log.warn("NO DRIVERS ARE FOUND");
            return;
        }

        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearByDrivers);
        if (bestDriver.isEmpty())
        {
            log.warn("COULD NOT FOUND A SUITABLE DRIVER");
            return;
        }

        NearByDriverResponse assigned = bestDriver.get();
        RideMatchedEvent matchedEvent = new RideMatchedEvent();
        matchedEvent.setRideId(event.getRideId());
        matchedEvent.setRiderId(event.getRiderId());
        matchedEvent.setDriverId(assigned.getDriverId());
        matchedEvent.setDriverLatitude(assigned.getLatitude());
        matchedEvent.setDriverLongitude(assigned.getLongitude());
        matchedEvent.setDistanceToPickupKm(assigned.getDistanceInKm());

        kafkaTemplate.send(RIDE_MATCHED_TOPIC,event.getRideId(),matchedEvent);
        log.info("RIDE MATCHED EVENT PUBLISHED");
    }
    private Optional<NearByDriverResponse> findBestDriver(
            List<NearByDriverResponse> drivers) {

        double distanceWeight = 0.7;
        double ratingWeight = 0.3;

        return drivers.stream()
                .max(Comparator.comparingDouble(driver -> {
                    double distanceScore = 1.0/(driver.getDistanceInKm() + 0.1);
                    double simulatedRating = 4.0 + Math.random();
                    return (distanceScore * distanceWeight)
                            + (simulatedRating * ratingWeight);
                }));
    }
}
