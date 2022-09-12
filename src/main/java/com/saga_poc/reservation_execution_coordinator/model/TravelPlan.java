package com.saga_poc.reservation_execution_coordinator.model;

import com.saga_poc.reservation_execution_coordinator.service.ReservationStepCoordinator;
import org.springframework.stereotype.Component;

@Component
public class TravelPlan {

    final ReservationStepCoordinator reservationStepCoordinator;

    public TravelPlan(ReservationStepCoordinator reservationStepCoordinator) {
        this.reservationStepCoordinator = reservationStepCoordinator;
    }


    public void reserve(Reservation reservation, ReservationStatus status) {
        System.out.println("making reservation");
    }

}
