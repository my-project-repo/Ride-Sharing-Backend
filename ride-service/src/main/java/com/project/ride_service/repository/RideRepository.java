package com.project.ride_service.repository;

import com.project.ride_service.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride,String> {
    List<Ride> findByRiderIdOrderByCreatedAtDesc (String riderId);
}
