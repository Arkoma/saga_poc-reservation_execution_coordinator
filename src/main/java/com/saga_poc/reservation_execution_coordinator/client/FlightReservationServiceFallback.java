package com.saga_poc.reservation_execution_coordinator.client;

import com.saga_poc.reservation_execution_coordinator.model.FlightReservation;
import com.saga_poc.reservation_execution_coordinator.model.FlightReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import com.saga_poc.reservation_execution_coordinator.service.ReservationStepCoordinator;
import org.springframework.stereotype.Component;

import static com.saga_poc.reservation_execution_coordinator.model.StatusEnum.CANCELED;

@Component
public class FlightReservationServiceFallback implements FlightReservationServiceClient {

    private ReservationStepCoordinator reservationStepCoordinator;

    public FlightReservationServiceFallback (ReservationStepCoordinator reservationStepCoordinator) {
        this.reservationStepCoordinator = reservationStepCoordinator;
    }

    @Override
    public FlightReservationResponse makeReservation(FlightReservation flightReservation) {
       final FlightReservationResponse response = new FlightReservationResponse();
       response.setStatus(CANCELED);
       return response;
    }
}
