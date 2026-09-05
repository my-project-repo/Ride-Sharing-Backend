package com.project.location_service.controller;

import com.project.location_service.dto.DriverLocationRequest;
import com.project.location_service.dto.NearByDriverResponse;
import com.project.location_service.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    // calls this method every 3 sec -> add if driver doesn't exist else update
    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation
            (@RequestBody DriverLocationRequest driverLocationRequest) {
        locationService.updateDriverLocation(driverLocationRequest);
        return ResponseEntity.ok("Driver Location Updated");
    }

    // matching service will call this when ride is requested
    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearByDriverResponse>> getNearByDriver
            (@RequestParam double latitude,
             @RequestParam double longitude,
             @RequestParam (defaultValue = "5.0") double radius){
        return ResponseEntity.ok(locationService.findNearByDrivers(latitude,longitude,radius));
    }

    @DeleteMapping("/drivers/{driverId}")
    public  ResponseEntity<String> removeDriver (@PathVariable String driverId)
    {
        locationService.removeDriver(driverId);
        return ResponseEntity.ok("Driver Removed");
    }


}
