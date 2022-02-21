package com.saga_poc.reservation_execution_coordinator.service;

import com.saga_poc.reservation_execution_coordinator.repository.ReservationStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationStepCoordinator {

    private final ReservationStatusRepository reservationStatusRepository;

    public ReservationStepCoordinator(ReservationStatusRepository reservationStatusRepository) {
        this.reservationStatusRepository = reservationStatusRepository;
    }
}
