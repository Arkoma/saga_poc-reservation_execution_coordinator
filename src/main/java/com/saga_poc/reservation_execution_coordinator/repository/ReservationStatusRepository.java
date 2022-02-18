package com.saga_poc.reservation_execution_coordinator.repository;

import com.saga_poc.reservation_execution_coordinator.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationStatusRepository extends JpaRepository<ReservationStatus, Long> {
    Optional<ReservationStatus> findByReservationId(long reservationId);
}
