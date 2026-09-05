package com.project.ride_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // matching token subscribes to this topic so any ride request -> kafka -> matching service

    @Bean
    public NewTopic rideRequestTopic ()
    {
        return TopicBuilder.name("ride.requested")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // ride service subscribes to this so when the drivers are matched ride service is notified

    @Bean
    public NewTopic rideMatchedTopic ()
    {
        return TopicBuilder.name("ride.matched")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /*
      The entire flow becomes ->
      Rider requests a ride -> Matching service is notified through Kafka
      Matching service calls Location service and fetches nearBy drivers and filters them
      After matching is done Matching service notifies it to the Ride Service
      Then ride service updates the ride
     */

}
