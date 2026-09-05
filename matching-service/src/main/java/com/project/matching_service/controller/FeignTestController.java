package com.project.matching_service.controller;

import com.project.matching_service.client.LocationServiceClient;
import com.project.matching_service.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class FeignTestController {

    private final LocationServiceClient locationServiceClient;

    @GetMapping("/nearby-drivers")
    public List<NearByDriverResponse> testNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radius) {

        log.info("Testing Feign client - Getting nearby drivers at lat: {}, lon: {}, radius: {}",
                latitude, longitude, radius);

        List<NearByDriverResponse> drivers = locationServiceClient.getNearByDrivers(latitude, longitude, radius);
        log.info("Found {} drivers", drivers.size());

        return drivers;
    }

}