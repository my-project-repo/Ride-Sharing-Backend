package com.project.ride_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rides")
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String riderId;

    private  String driverId;

    @Column(nullable = false)
    private double pickUpLatitude;
    @Column(nullable = false)
    private double pickUpLongitude;
    @Column(nullable = false)
    private String pickUpAddress;

    @Column(nullable = false)
    private double dropLatitude;
    @Column(nullable = false)
    private double dropLongitude;
    @Column(nullable = false)
    private String dropAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    private double estimatedFare;
    private double actualFare;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

}
