package com.saga_poc.reservation_execution_coordinator.client;

import com.saga_poc.reservation_execution_coordinator.model.FlightReservation;
import com.saga_poc.reservation_execution_coordinator.model.FlightReservationResponse;
import org.springframework.stereotype.Component;

import static com.saga_poc.reservation_execution_coordinator.model.StatusEnum.CANCELED;

@Component
public class FlightReservationServiceFallback implements FlightReservationServiceClient {

    @Override
    public FlightReservationResponse makeReservation(FlightReservation flightReservation) {
        final FlightReservationResponse response = new FlightReservationResponse();
        response.setStatus(CANCELED);
        return response;
    }
}
