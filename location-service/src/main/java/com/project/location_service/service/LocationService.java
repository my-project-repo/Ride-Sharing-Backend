package com.project.location_service.service;

import com.project.location_service.dto.DriverLocationRequest;
import com.project.location_service.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    private final RedisTemplate<String,String> redisTemplate;
    private static final String DRIVER_GEO_KEY = "drivers:locations";
    public void updateDriverLocation(DriverLocationRequest driverLocationRequest) {
        log.info("Updating Location for Driver {}: lat={}, long={} ",
                driverLocationRequest.getDriverId(),
                driverLocationRequest.getLatitude(),
                driverLocationRequest.getLongitude());

        // first longitude then latitude as it is the standard for geo spacial
        Point point = new Point(
                driverLocationRequest.getLongitude(),
                driverLocationRequest.getLatitude()
        );

        redisTemplate.opsForGeo().add(
                DRIVER_GEO_KEY,
                point,
                driverLocationRequest.getDriverId()
        );

        log.info("Updated Location for Driver {}: lat={}, long={} ",
                driverLocationRequest.getDriverId(),
                driverLocationRequest.getLatitude(),
                driverLocationRequest.getLongitude());
        
    }

    public List<NearByDriverResponse> findNearByDrivers(double latitude, double longitude, double radius) {
        log.info("Finding Drivers near lat={}, long={}, within {} radius",latitude,longitude,radius);
        Circle circle = new Circle(
                new Point(longitude,latitude),
                new Distance(radius, Metrics.KILOMETERS)
        );

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =redisTemplate.opsForGeo().radius(DRIVER_GEO_KEY,circle,
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeCoordinates()
                        .includeDistance()
                        .sortAscending()
                        .limit(10)
        );
        List<NearByDriverResponse> list = new ArrayList<>();
        if (results != null) {
            results.getContent().forEach(result -> {
                RedisGeoCommands.GeoLocation<String> content = result.getContent();
                list.add(new NearByDriverResponse(
                        content.getName(),
                        content.getPoint().getY(),
                        content.getPoint().getX(),
                        result.getDistance().getValue()
                ));
            });
        }
        log.info("Found drivers nearby {}",list.size());
        return list;
    }

    public void removeDriver(String driverId) {
        log.info("Removing driver {}",driverId);
        redisTemplate.opsForGeo().remove(DRIVER_GEO_KEY,driverId);
    }
}
