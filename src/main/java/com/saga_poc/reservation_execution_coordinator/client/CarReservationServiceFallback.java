package com.saga_poc.reservation_execution_coordinator.client;

import com.saga_poc.reservation_execution_coordinator.model.CarReservation;
import com.saga_poc.reservation_execution_coordinator.model.CarReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import org.springframework.stereotype.Component;

@Component
public class CarReservationServiceFallback implements CarReservationServiceClient {
    @Override
    public CarReservationResponse makeReservation(CarReservation carReservation) {
        final CarReservationResponse response = new CarReservationResponse();
        response.setStatus(StatusEnum.CANCELED);
        return response;
    }

    @Override
    public void cancelReservation(Long id) {
        // TODO: implement retry strategy
    }
}
