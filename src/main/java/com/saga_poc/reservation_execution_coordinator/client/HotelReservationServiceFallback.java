package com.saga_poc.reservation_execution_coordinator.client;

import com.saga_poc.reservation_execution_coordinator.model.HotelReservation;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservationResponse;
import org.springframework.stereotype.Component;

import static com.saga_poc.reservation_execution_coordinator.model.StatusEnum.CANCELED;

@Component
public class HotelReservationServiceFallback implements HotelReservationServiceClient {

    @Override
    public HotelReservationResponse makeReservation(HotelReservation hotelReservation) {
        final HotelReservationResponse response = new HotelReservationResponse();
        response.setStatus(CANCELED);
        return response;
    }

    @Override
    public void cancelReservation(Long id) {
        // TODO: implement retry strategy
    }
}
